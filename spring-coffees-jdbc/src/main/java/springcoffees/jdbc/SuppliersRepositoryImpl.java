package springcoffees.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import springcoffees.domain.Supplier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class SuppliersRepositoryImpl implements SuppliersRepository {

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public SuppliersRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        jdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("suppliers")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Supplier> findAll() {
        return jdbc.query("SELECT id, name, email, address FROM suppliers",
                this::mapFromRowToSupplier);
    }

    @Override
    public List<Supplier> findAllOrderByName() {
        return jdbc.query("SELECT id, name, email, address FROM suppliers ORDER BY name",
                this::mapFromRowToSupplier);
    }

    @Override
    public Supplier save(Supplier supplier) {
        int oldId = supplier.getId();
        String name = supplier.getName();
        String email = supplier.getEmail();
        String address = supplier.getAddress();
        try {
            if (oldId == 0) {
                // insert new record
                Map<String, Object> params = Map.of("id", "DEFAULT",
                        "name", name, "email", email, "address", address);
                int newId = jdbcInsert.executeAndReturnKey(params).intValue();
                supplier.setId(newId);
            } else {
                // update existing record
                jdbc.update("UPDATE suppliers SET name = ?, email = ?, address = ? WHERE id = ?",
                        name, email, address, oldId);
            }
        } catch (DataIntegrityViolationException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DataIntegrityViolationException(
                    String.format("Supplier with '%s' already exists. You can't add or update " +
                            "supplier with duplicate e-mail", email));
        }
        return supplier;
    }

    @Override
    public void delete(Supplier supplier) {
        try {
            jdbc.update("DELETE FROM suppliers WHERE id = ?", supplier.getId());
        } catch (DataIntegrityViolationException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DataIntegrityViolationException(
                    String.format("You can't delete supplier '%s' while there are still records " +
                            "referencing it in the 'Coffees' table", supplier.getName()));
        }
    }

    // a helper RowMapper implementation (when used locally as a method reference)
    private Supplier mapFromRowToSupplier(ResultSet rs, int rowNum) throws SQLException {
        return new Supplier(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("address"));
    }

}
