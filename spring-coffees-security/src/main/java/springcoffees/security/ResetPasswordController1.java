package springcoffees.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springcoffees.jdbc.UsersRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/reset-password/step-1")
public class ResetPasswordController1 {

    private final UsersRepository usersRepo;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public ResetPasswordController1(UsersRepository usersRepo, JavaMailSender mailSender) {
        this.usersRepo = usersRepo;
        this.mailSender = mailSender;
    }

    @GetMapping
    public String showResetPasswordForm1(Model model) {
        model.addAttribute("resetForm1", new ResetPasswordForm1());
        return "reset-1";
    }

    @PostMapping
    public String processResetPasswordFrom1(@Valid @ModelAttribute("resetForm1") ResetPasswordForm1 form,
                                            Errors errors, Model model, HttpServletRequest request) {
        if (!errors.hasErrors()) {
            String email = form.getEmail();
            Optional<String> optionalToken  = usersRepo.getNewResetTokenFor(email);
            if (optionalToken.isPresent()) {
                // send reset link with new token to user's e-mail
                MimeMessage message = constructHtmlMessage(optionalToken.get(), email, request);
                mailSender.send(message);
                model.addAttribute("status", "RESET_LINK_SENT");
                return "confirmation";
            } else {
                // add error on failure to get token for the given e-mail
                ((AbstractBindingResult) errors).addError(new FieldError("resetForm1",
                        "email", email, false, null, null,
                        String.format("User with e-mail '%s' not found or disabled", email)));
            }
        }
        return "reset-1";
    }

    private MimeMessage constructHtmlMessage(String token, String userEmail, HttpServletRequest request) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        String resetLink = request.getScheme() + "://" + request.getHeader("host")
                + "/reset-password/step-2/" + token;
        String html = String.format("We got a request to reset your Spring Coffees password.<br>" +
                "Please, follow this link to change your password:<br><br><a href='%1$s'>%1$s</a><br><br>" +
                "or ignore this message and your password won't be changed.", resetLink);
        try {
            mimeMessage.setContent(html, "text/html");
            helper.setTo(userEmail);
            helper.setSubject("Spring Coffees Reset Password Link");
            helper.setFrom(senderEmail);
        } catch (MessagingException e) {
            throw new RuntimeException("Reset Password Link creation failure", e);
        }
        return mimeMessage;
    }

}
