package springcoffees.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import springcoffees.domain.Sale;
import springcoffees.domain.SalesPage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SalesRepositoryImpl implements SalesRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public SalesRepositoryImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SalesPage findAllByOrderByDateTime(MapSqlParameterSource namedParams, Pageable pageable) {

        // construct base query
        StringBuilder baseQuery = new StringBuilder("WITH tmp (sale_id, coffee_tuple, supplier_id, " +
                "supplier_name, sale_manager, date_time, sale_quantity, sale_sum) AS (" +
                "       SELECT s.id, CONCAT(c.name, ' (', c.origin, ')'), c.supplier_id, " +
                "       sup.name, s.manager, s.datetime, s.sale_quantity, s.sale_sum " +
                "       FROM sales s LEFT JOIN coffees c ON s.coffee_id = c.id " +
                "       LEFT JOIN suppliers sup ON c.supplier_id = sup.id" +
                ") SELECT * FROM tmp WHERE 1 = 1");
        if (namedParams.hasValue("coffee_tuple"))
            baseQuery.append(" AND coffee_tuple = :coffee_tuple");
        if (namedParams.hasValue("supplier_id"))
            baseQuery.append(" AND supplier_id = :supplier_id");
        if (namedParams.hasValue("sale_manager"))
            baseQuery.append(" AND sale_manager = :sale_manager");
        if (namedParams.hasValue("start_date"))
            baseQuery.append(" AND date_time >= :start_date");
        if (namedParams.hasValue("end_date"))
            baseQuery.append(" AND date_time <= :end_date");

        // query #1: fetching totals (count, quantity and sum)
        String sqlTotals = baseQuery.toString().replace("SELECT *",
                "SELECT COUNT(sale_id), SUM(sale_quantity), SUM(sale_sum)");
        SalesPage.Builder pageBuilder = SalesPage.builder();
        jdbc.query(sqlTotals, namedParams, (rs) -> {
            pageBuilder.totalCount(rs.getInt(1));
            pageBuilder.totalQuantity(rs.getLong(2));
            pageBuilder.totalSum(rs.getBigDecimal(3));
        });

        // rewind pageable if it exceeds total count
        int totalCount = pageBuilder.getTotalCount();
        if (pageable.getOffset() + 1 > totalCount) {
            pageable = pageable.first();
        }

        // query #2: fetching slice of sales for current page
        baseQuery.append(" ORDER BY date_time");
        Sort.Order orderFor = pageable.getSort().getOrderFor("dateTime");
        if ((orderFor != null) && orderFor.getDirection().isDescending())
            baseQuery.append(" DESC");
        baseQuery.append(" LIMIT :limit OFFSET :offset");
        namedParams.addValue("limit", pageable.getPageSize());
        namedParams.addValue("offset", pageable.getOffset());
        List<Sale> list = jdbc.query(baseQuery.toString(), namedParams, this::mapFromRowToSale);

        return pageBuilder.salesList(list).pageable(pageable).build();
    }

    // a helper RowMapper implementation (when used locally as a method reference)
    private Sale mapFromRowToSale(ResultSet rs, int rowNum) throws SQLException {
        return new Sale(rs.getInt("sale_id"),
                rs.getString("coffee_tuple"),
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getString("sale_manager"),
                rs.getTimestamp("date_time").toLocalDateTime(),
                rs.getInt("sale_quantity"),
                rs.getBigDecimal("sale_sum"));
    }

    @Override
    public void deleteSelected(int[] saleIds) {
        String sql = "DELETE FROM sales WHERE id IN (:ids)";
        List<Integer> idsList = Arrays.stream(saleIds).boxed().collect(Collectors.toList());
        MapSqlParameterSource params = new MapSqlParameterSource("ids", idsList);
        jdbc.update(sql, params);
    }

}
