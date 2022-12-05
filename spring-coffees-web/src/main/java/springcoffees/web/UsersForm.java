package springcoffees.web;

import lombok.Data;
import springcoffees.domain.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
class UsersForm {

    @NotBlank(message = "Username must not be empty")
    @Pattern(regexp = "(?!admin$).*", message = "Operation is not supported for the reserved 'admin' user")
    private String username;

    private String firstName;
    private String lastName;
    private String email;

    @NotNull(message = "Enabled status must not be empty")
    private User.Enabled enabled;
    @NotNull(message = "Authority value must not be empty")
    private User.Authority authority;

    private String formAction;

}
