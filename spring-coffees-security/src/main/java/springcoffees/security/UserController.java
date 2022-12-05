package springcoffees.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springcoffees.domain.User;
import springcoffees.jdbc.UsersRepository;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UsersRepository usersRepo;

    @Autowired
    public UserController(UsersRepository usersRepo) {
        this.usersRepo = usersRepo;
    }

    @GetMapping()
    public String showUserProfile(Model model, Principal principal) {
        User user = usersRepo.findByUsername(principal.getName());
        model.addAttribute("currentUser", user);
        return "user-profile";
    }

    @GetMapping("/update")
    public String showUserUpdateForm(Model model, Principal principal) {
        User user = usersRepo.findByUsername(principal.getName());
        RegistrationForm userUpdateForm = new RegistrationForm();
        userUpdateForm.setFirstName(user.getFirstName());
        userUpdateForm.setLastName(user.getLastName());
        userUpdateForm.setEmail(user.getEmail());
        model.addAttribute("userUpdateForm", userUpdateForm);
        return "user-update";
    }

    @PostMapping("/update")
    public String processUserUpdateForm(@Valid @ModelAttribute("userUpdateForm") RegistrationForm form,
                                        Errors errors, Model model, Principal principal) {
        if (!errors.hasErrors()) {
            String username = principal.getName();
            String firstName = form.getFirstName();
            String lastName = form.getLastName();
            String email = form.getEmail();
            try {
                usersRepo.updateNameAndEmailByUsername(username, firstName, lastName, email);
                model.addAttribute("status", "USER_UPDATED");
                return "confirmation";
            } catch (DuplicateKeyException ex) {
                AbstractBindingResult bindingResults = (AbstractBindingResult) errors;
                bindingResults.addError(new FieldError("userUpdateForm",
                        "email", email, false, null, null,
                        String.format("E-mail '%s' already in use (try another one)", email)));
            }
        }
        return "user-update";
    }

}
