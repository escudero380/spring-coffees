package springcoffees.security;

import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import springcoffees.domain.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
class RegistrationForm {

    interface PasswordAware {
        // validation group marker interface
    }

    @NotBlank(message = "Username must not be empty")
    @Pattern(regexp = "^[a-zA-Z][a-z\\d]*",
            message = "Username may contain only letters and digits and must begin with a letter")
    private String username;

    @NotBlank(message = "First name must not be empty")
    private String firstName;
    @NotBlank(message = "Last name must not be empty")
    private String lastName;

    @NotBlank(groups = {PasswordAware.class}, message = "Password must not be empty")
    @Size(groups = {PasswordAware.class}, min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "User e-mail must not be empty")
    @Email(message = "Please, provide valid user e-mail")
    private String email;

    // a helper method to convert registration form into user object
    User toUserWithPasswordEncoder(PasswordEncoder encoder) {
        return new User(username, firstName, lastName, encoder.encode(password),
                false, User.Authority.NEWCOMER, email, null, null);
    }

}
