package springcoffees.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import springcoffees.domain.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class UsersRepositoryImpl implements UsersRepository {

    private final JdbcTemplate jdbc;

    @Autowired
    public UsersRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<User> findByEnabledAndAuthorityOrderBy(User.Enabled enabled, User.Authority authority,
                                                       User.OrderBy orderBy) {
        String sql = "SELECT * FROM users WHERE 1 = 1";
        sql += (enabled != null) ? String.format(" AND enabled = %s", enabled) : "";
        sql += (authority != null) ? String.format(" AND authority = '%s'", authority) : "";
        sql += (orderBy != null) ? String.format(" ORDER BY %s", orderBy.getDbField()) : "";
        return jdbc.query(sql, this::mapFromRowToUser);
    }

    @Override
    public List<User> findAllOrderBy(User.OrderBy orderBy) {
        return findByEnabledAndAuthorityOrderBy(null, null, orderBy);
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            return jdbc.queryForObject(sql, this::mapFromRowToUser, username);
        } catch (EmptyResultDataAccessException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DataIntegrityViolationException(
                    String.format("Username '%s' no longer exists!", username));
        }
    }

    @Override
    public User save(User user) {

        // step #1: check for duplicate fields
        String sql = "SELECT * FROM users WHERE username = ? OR email = ? FOR UPDATE";
        Map<String, String> duplicates = new HashMap<>();
        jdbc.query(sql, (rs) -> {
            if (rs.getString("username").equals(user.getUsername()))
                duplicates.put("username", user.getUsername());
            if (rs.getString("email").equals(user.getEmail()))
                duplicates.put("email", user.getEmail());
        }, user.getUsername(), user.getEmail());

        if (!duplicates.isEmpty())
            throw DuplicateFieldsException.constructFrom(duplicates);

        // step #2: update if no duplicate fields
        jdbc.update("INSERT INTO users VALUES(?, ?, ?, ?, ?, ?, ?, NULL, DEFAULT)",
                user.getUsername(), user.getFirstName(), user.getLastName(), user.getPassword(),
                user.isEnabled(), user.getAuthority().toString(), user.getEmail());
        return user;
    }

    public void updateNameAndEmailByUsername(String username, String firstName, String lastName, String email) {
        try {
            jdbc.update("UPDATE users SET first_name = ?, last_name = ?, email = ? WHERE username = ?",
                    firstName, lastName, email, username);
        } catch (DuplicateKeyException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DuplicateKeyException(String.format("User with '%s' already exists. You can't update " +
                    "user with repeating e-mail", email));
        }
    }

    public void updateEnabledAndAuthorityByUsername(String username, User.Enabled enabled,
                                                    User.Authority authority) {
        jdbc.update("UPDATE users SET enabled = ?, authority = ? WHERE username = ?",
                enabled.toString(), authority.toString(), username);
    }

    @Override
    public void deleteByUsername(String username) {
        try {
            jdbc.update("DELETE FROM users WHERE username = ?", username);
        } catch (DataIntegrityViolationException ex) {
            // mask technical exception details with custom user-friendly report
            throw new DataIntegrityViolationException(
                    String.format("You can't delete user '%s' while there are still records " +
                            "referencing it in the 'Sales' table", username));
        }
    }

    @Override
    public Optional<String> getNewResetTokenFor(String email) {
        String newToken = UUID.randomUUID().toString();
        String sql = "UPDATE users SET reset_token = ?, exp_before = ? WHERE email = ? AND enabled = TRUE";
        int update = jdbc.update(sql, newToken, LocalDateTime.now().plusHours(24), email);
        return (update == 1) ? Optional.of(newToken)
                : Optional.empty();
    }

    @Override
    public Optional<User> updatePasswordByToken(String password, String token) {
        //step #1: fetch user by token
        String sql = "SELECT * FROM users WHERE reset_token = ? AND exp_before > CURRENT_TIMESTAMP FOR UPDATE";
        try {
            User user = jdbc.queryForObject(sql, this::mapFromRowToUser, token);
            Objects.requireNonNull(user);
            //step #2: update user's password
            //note: we also make current token expired, so that no one can reuse it
            sql = "UPDATE users SET password = ?, exp_before = CURRENT_TIMESTAMP WHERE username = ?";
            jdbc.update(sql, password, user.getUsername());
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    // a helper RowMapper implementation (when used locally as a method reference)
    private User mapFromRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getString("username"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getBoolean("enabled"),
                User.Authority.valueOf(rs.getString("authority")),
                rs.getString("email"),
                rs.getString("reset_token"),
                rs.getTimestamp("exp_before").toLocalDateTime());
    }

}
