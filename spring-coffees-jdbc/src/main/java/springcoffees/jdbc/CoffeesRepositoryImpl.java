package springcoffees.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import springcoffees.domain.Coffee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntConsumer;

@Repository
public class CoffeesRepositoryImpl implements CoffeesRepository {
    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public CoffeesRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.jdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("coffees")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Coffee> findBySupplierIdOrderBy(int supplierId, Coffee.OrderBy orderBy) {
        String sql = "SELECT c.id, c.name, c.origin, c.supplier_id, c.current_price, c.stock, " +
                "s.name supplier_name FROM coffees c LEFT JOIN suppliers s ON c.supplier_id = s.id ";
        sql += (supplierId != 0) ? String.format(" WHERE s.id = %s", supplierId) : "";
        sql += String.format(" ORDER BY %s", orderBy.getDbField());
        return jdbc.query(sql, this::mapFromRowToCoffee);
    }

    @Override
    public Coffee save(Coffee coffee) {
        int oldId = coffee.getId();
        String name = coffee.getName();
        String origin = coffee.getOrigin();
        int supplierID = coffee.getSupplierId();
        BigDecimal currentPrice = coffee.getCurrentPrice();
        try {
            if (oldId == 0) {
                // insert new record
                Map<String, Object> params = Map.of("name", name, "origin", origin,
                        "supplier_id", supplierID, "current_price", currentPrice, "stock", 0);
                int newId = jdbcInsert.executeAndReturnKey(params).intValue();
                coffee.setId(newId);
            } else {
                // update existing record
                jdbc.update("UPDATE coffees SET name = ?, origin = ?, supplier_id = ?, current_price = ? " +
                        "WHERE id = ? ", name, origin, supplierID, currentPrice, oldId);
            }
        } catch (DataIntegrityViolationException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DataIntegrityViolationException("This item can't be updated or added " +
                    "to the 'Coffees' table, possibly because the corresponding coffee supplier " +
                    "no longer exists in the 'Suppliers' table");
        }
        return coffee;
    }

    @Override
    public void delete(Coffee coffee) {
        try {
            jdbc.update("DELETE FROM coffees WHERE id = ? ", coffee.getId());
        } catch (DataIntegrityViolationException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DataIntegrityViolationException(
                    String.format("You can't delete coffee '%s' while there are still records " +
                            "referencing it in the 'Sales' table", coffee.getName()));
        }
    }

    @Override
    public boolean sellOrElseAccept(Coffee coffee, int quantity, String manager,
                                    IntConsumer stockBeforeSellingAcceptor) {
        if (quantity == 0) return true;

        // step #1: check if we have enough coffee in stock
        String sql = "SELECT stock FROM coffees WHERE id = ? FOR UPDATE";
        int stockAvailable;
        try {
            stockAvailable = Objects.requireNonNull(jdbc.queryForObject(sql, Integer.class, coffee.getId()));
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new DataIntegrityViolationException(String.format(
                    "Failed to accomplish operation, possibly because coffee item '%s (%s)' was " +
                            "removed from the 'Coffees' table.", coffee.getName(), coffee.getOrigin()));
        }
        int stockAfterSale = stockAvailable - quantity;
        if (stockAfterSale < 0) {
            // when there is not enough coffee in stock to accomplish sale operation - provide
            // current stock info, so that the client code can properly handle this situation
            stockBeforeSellingAcceptor.accept(stockAvailable);
            return false;
        }

        // step #2: insert new record into sales table
        sql = "INSERT INTO sales (coffee_id, manager, datetime, sale_quantity, sale_sum) " +
                "VALUES( ?, ?, ?, ?, ?)";
        BigDecimal saleSum = coffee.getCurrentPrice()
                .multiply(BigDecimal.valueOf(quantity)
                        .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
        jdbc.update(sql, coffee.getId(), manager, LocalDateTime.now(), quantity, saleSum);

        // step #3: update coffee stock after sale
        sql = "UPDATE coffees SET stock = ? WHERE id = ?";
        jdbc.update(sql, stockAfterSale, coffee.getId());
        return true;
    }


    @Override
    public void buy(Coffee coffee, int quantity) {
        if (quantity == 0) return;

        // step #1: check for current stock
        String sql = "SELECT stock FROM coffees WHERE id = ? FOR UPDATE";
        int stockAvailable;
        try {
            stockAvailable = Objects.requireNonNull(jdbc.queryForObject(sql, Integer.class, coffee.getId()));
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new DataIntegrityViolationException(String.format(
                    "Failed to accomplish operation, possibly because coffee item '%s (%s)' was " +
                            "removed form the 'Coffees' table.", coffee.getName(), coffee.getOrigin()));
        }
        int stockAfterBuying = stockAvailable + quantity;
        if (stockAfterBuying > 1_000_000)
            throw new DataIntegrityViolationException(String.format(
                    "The current stock for coffee '%s (%s)' is %s, and you're going to buy more %s. " +
                            "We probably don't need so much of it. Please, try to sell some first. ",
                    coffee.getName(), coffee.getOrigin(), stockAvailable, quantity));

        // step #2: complete the transaction
        jdbc.update("UPDATE coffees SET stock = ? WHERE id = ? ",
                stockAfterBuying, coffee.getId());
    }

    @Override
    public List<String> findCoffeeTuplesDistinct() {
        String sql = "SELECT DISTINCT CONCAT(name, ' (', origin, ')') AS coffee_tuple " +
                "FROM coffees ORDER BY coffee_tuple";
        return jdbc.queryForList(sql, String.class);
    }

    // a helper RowMapper implementation (when used locally as a method reference)
    private Coffee mapFromRowToCoffee(ResultSet rs, int rowNum) throws SQLException {
        return new Coffee(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("origin"),
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getBigDecimal("current_price"),
                rs.getInt("stock"));
    }

}
