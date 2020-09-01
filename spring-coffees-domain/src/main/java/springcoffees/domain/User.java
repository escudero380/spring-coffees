package springcoffees.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class User {

    private final String username;
    private final String firstName;
    private final String lastName;
    private final String password;
    private final boolean enabled;
    private final Authority authority;
    private final String email;
    private final String resetToken;        // used in reset password procedure
    private final LocalDateTime expBefore;  // expiry time for token (not user)

    public enum Enabled {TRUE, FALSE}

    public enum Authority {ADMIN, MANAGER, NEWCOMER}

    public enum OrderBy {

        USERNAME("username", "Username"),
        FIRST_NAME("first_name", "First name"),
        LAST_NAME("last_name", "Last name"),
        EMAIL("email", "E-mail");

        private final String dbField;
        private final String displayValue;

        OrderBy(String dbField, String displayValue) {
            this.dbField = dbField;
            this.displayValue = displayValue;
        }

        public String getDbField() {
            return dbField;
        }

        public String getDisplayValue() {
            return displayValue;
        }

    }

}
