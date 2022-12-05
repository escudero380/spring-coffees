package springcoffees.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import springcoffees.domain.Stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatsRepositoryImpl implements StatsRepository {

    private final JdbcTemplate jdbc;

    @Autowired
    public StatsRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Stats topSelling(Stats.TopSellers sellers, LocalDate startDate, LocalDate endDate, int limit) {
        switch (sellers) {
            case SUPPLIERS:
                return topSellingSuppliers(startDate, endDate, limit);
            case MANGERS:
                return topSellingManagers(startDate, endDate, limit);
            default:
                return topSellingCoffees(startDate, endDate, limit);
        }
    }

    @Override
    public Stats topSellingCoffees(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String sql = "WITH tmp (coffee, quantity, sum) AS ( " +
                "SELECT coffee_id, SUM(sale_quantity), SUM(sale_sum) " +
                "FROM sales WHERE datetime BETWEEN ? AND ? " +
                "GROUP BY coffee_id" +
                "   ) SELECT c.name, c.origin, s.name, tmp.quantity, tmp.sum FROM tmp " +
                "       LEFT JOIN coffees c ON tmp.coffee = c.id" +
                "       LEFT JOIN suppliers s ON c.supplier_id = s.id " +
                "       ORDER BY tmp.sum DESC LIMIT ?";
        List<String> headers = List.of("#", "Coffee Name", "Coffee Origin", "Supplier Name",
                "Average Price", "Quantity Sold", "Sum");
        List<List<String>> results = new ArrayList<>();
        final long[] quantityAcc = new long[]{0};
        final BigDecimal[] sumAcc = new BigDecimal[]{BigDecimal.ZERO};
        jdbc.query(sql, new Object[]{startDateTime, endDateTime, limit}, (rs) -> {
            String num = rs.getRow() + "";
            String coffee = rs.getString(1);
            String origin = rs.getString(2);
            String supplier = rs.getString(3);
            long quantityValue = rs.getLong(4);
            BigDecimal sumValue = rs.getBigDecimal(5);
            BigDecimal averagePriceValue = sumValue.multiply(BigDecimal.valueOf(1000)
                    .divide(BigDecimal.valueOf(quantityValue), 2, RoundingMode.HALF_UP));
            String quantity = String.format("%,d", quantityValue);
            String sum = String.format("%,.2f", sumValue);
            String price = String.format("%,.2f", averagePriceValue);
            results.add(List.of(num, coffee, origin, supplier, price, quantity, sum));
            // accumulate totals
            quantityAcc[0] += quantityValue;
            sumAcc[0] = sumAcc[0].add(sumValue);
        });
        return new Stats(headers, results, quantityAcc[0], sumAcc[0]);
    }

    @Override
    public Stats topSellingSuppliers(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String sql = "WITH tmp (supplier, quantity, sum) AS (" +
                "SELECT c.supplier_id, SUM(s.sale_quantity), SUM(s.sale_sum) " +
                "FROM sales s LEFT JOIN coffees c ON s.coffee_id = c.id " +
                "WHERE datetime BETWEEN ? AND ? " +
                "GROUP BY c.supplier_id" +
                "   ) SELECT suppliers.name, suppliers.address, tmp.quantity, tmp.sum " +
                "       FROM tmp LEFT JOIN suppliers on tmp.supplier = suppliers.id " +
                "       ORDER BY tmp.sum DESC LIMIT ?";
        List<String> headers = List.of("#", "Supplier Name", "Supplier Address", "Quantity Sold", "Sum");
        List<List<String>> results = new ArrayList<>();
        final long[] quantityAcc = new long[]{0};
        final BigDecimal[] sumAcc = new BigDecimal[]{BigDecimal.ZERO};
        jdbc.query(sql, new Object[]{startDateTime, endDateTime, limit}, (rs) -> {
            String num = rs.getRow() + "";
            String supplier = rs.getString(1);
            String address = rs.getString(2);
            long quantityValue = rs.getLong(3);
            BigDecimal sumValue = rs.getBigDecimal(4);
            String quantity = String.format("%,d", quantityValue);
            String sum = String.format("%,.2f", sumValue);
            results.add(List.of(num, supplier, address, quantity, sum));
            // accumulate totals
            quantityAcc[0] += quantityValue;
            sumAcc[0] = sumAcc[0].add(sumValue);
        });
        return new Stats(headers, results, quantityAcc[0], sumAcc[0]);
    }

    @Override
    public Stats topSellingManagers(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        String sql = "WITH tmp(manager, quantity, sum) AS (" +
                "SELECT manager, SUM(sale_quantity), SUM(sale_sum) " +
                "FROM sales WHERE datetime BETWEEN ? AND ? " +
                "GROUP BY manager" +
                "   ) SELECT tmp.manager, u.first_name, u.last_name, tmp.quantity, tmp.sum " +
                "       FROM tmp LEFT JOIN users u on tmp.manager = u.username" +
                "       ORDER BY tmp.sum DESC LIMIT ?";
        List<String> headers = List.of("#", "Username", "First Name", "Last Name", "Quantity Sold", "Sum");
        List<List<String>> results = new ArrayList<>();
        final long[] quantityAcc = new long[]{0};
        final BigDecimal[] sumAcc = new BigDecimal[]{BigDecimal.ZERO};
        jdbc.query(sql, new Object[]{startDateTime, endDateTime, limit}, (rs) -> {
            String num = rs.getRow() + "";
            String username = rs.getString(1);
            String firstName = rs.getString(2);
            String lastName = rs.getString(3);
            long quantityValue = rs.getLong(4);
            BigDecimal sumValue = rs.getBigDecimal(5);
            String quantity = String.format("%,d", quantityValue);
            String sum = String.format("%,.2f", sumValue);
            results.add(List.of(num, username, firstName, lastName, quantity, sum));
            // accumulate totals
            quantityAcc[0] += quantityValue;
            sumAcc[0] = sumAcc[0].add(sumValue);
        });
        return new Stats(headers, results, quantityAcc[0], sumAcc[0]);
    }

}
