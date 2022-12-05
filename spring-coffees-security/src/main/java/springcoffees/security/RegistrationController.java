package springcoffees.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springcoffees.domain.User;
import springcoffees.jdbc.DuplicateFieldsException;
import springcoffees.jdbc.UsersRepository;

import javax.validation.groups.Default;
import java.util.Map;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final UsersRepository usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public RegistrationController(UsersRepository usersRepo, PasswordEncoder encoder,
                                  JavaMailSender mailSender) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = encoder;
        this.mailSender = mailSender;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "registration";
    }

    @PostMapping
    public String processRegistrationFrom(@Validated({Default.class, RegistrationForm.PasswordAware.class})
                                          @ModelAttribute RegistrationForm form,
                                          Errors errors, Model model) {
        if (!errors.hasErrors()) {
            User user = form.toUserWithPasswordEncoder(passwordEncoder);
            try {
                usersRepo.save(user);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(form.getEmail());
                message.setFrom(senderEmail);
                message.setSubject("Spring Coffees Registration");
                message.setText(String.format("You've created an account on Spring Coffees with " +
                        "the following credentials:%n%nUsername - %s%nPassword - %s%n%n" +
                        "Remember them! When our administrator confirms your " +
                        "registration - subject to %nfurther notification - you can use this " +
                        "username and password for login.", form.getUsername(), form.getPassword()));
                mailSender.send(message);
                model.addAttribute("status", "ACCOUNT_CREATED");
                return "confirmation";
            } catch (DuplicateFieldsException ex) {
                addToErrors(errors, ex.getDuplicateFields());
            }
        }
        return "registration";
    }

    private void addToErrors(Errors errors, Map<String, String> duplicateFields) {
        AbstractBindingResult bindingResults = (AbstractBindingResult) errors;
        duplicateFields.forEach((field, value) -> {
            if (field.equals("username"))
                bindingResults.addError(new FieldError("registrationForm",
                        "username", value, false, null, null,
                        String.format("Username '%s' already in use (try another one)", value)));
            if (field.equals("email"))
                bindingResults.addError(new FieldError("registrationForm",
                        "email", value, false, null, null,
                        String.format("E-mail '%s' already in use (try another one)", value)));
        });
    }
}
