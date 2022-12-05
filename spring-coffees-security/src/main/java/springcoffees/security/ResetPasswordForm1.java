package springcoffees.security;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
class ResetPasswordForm1 {

    @NotBlank(message = "E-mail must not be empty")
    @Email(message = "Invalid e-mail")
    private String email;

}
