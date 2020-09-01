package springcoffees.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import springcoffees.domain.User;
import springcoffees.jdbc.UsersRepository;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/reset-password/step-2")
public class ResetPasswordController2 {

    private final UsersRepository usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public ResetPasswordController2(UsersRepository usersRepo, PasswordEncoder passwordEncoder,
                                    JavaMailSender mailSender) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @GetMapping("/{token}")
    public String showResetPasswordFrom2(@PathVariable String token, Model model) {
        try {
            // check if token is of valid UUID format
            UUID uuid = UUID.fromString(token);
        } catch (IllegalArgumentException ex) {
            return "forward:/error/404";
        }
        model.addAttribute("resetForm2", new ResetPasswordForm2());
        model.addAttribute("token", token);
        return "reset-2";
    }

    @PostMapping()
    public String processResetPasswordForm2(@Valid @ModelAttribute("resetForm2") ResetPasswordForm2 form,
                                            Errors errors, Model model) {
        if (!errors.hasErrors()) {
            Optional<User> optional = usersRepo.updatePasswordByToken(
                    passwordEncoder.encode(form.getPassword()), form.getToken());
            if (optional.isPresent()) {
                emailCredentials(optional.get(), form.getPassword());
                model.addAttribute("status", "PASSWORD_SAVED");
                return "confirmation";
            } else {
                model.addAttribute("resetError", new Object());
            }
        }
        return "reset-2";
    }

    private void emailCredentials(User user, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(senderEmail);
        message.setSubject("Spring Coffees Password Reset");
        message.setText(String.format("Your password for Spring Coffees was reset.%n" +
                        "Use these updated credentials for login:%n%n" +
                        "Username - %s%nPassword - %s%n%n",
                user.getUsername(), password));
        mailSender.send(message);
    }
}
