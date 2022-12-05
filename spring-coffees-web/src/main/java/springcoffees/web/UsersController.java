package springcoffees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springcoffees.domain.User;
import springcoffees.jdbc.UsersRepository;

import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/users")
@SessionAttributes("usersFilters")
public class UsersController {

    private final UsersRepository usersRepo;
    private final SessionRegistry sessionRegistry;

    @Autowired
    public UsersController(UsersRepository usersRepo, SessionRegistry sessionRegistry) {
        this.usersRepo = usersRepo;
        this.sessionRegistry = sessionRegistry;
    }

    @ModelAttribute("usersFilters")
    Map<String, Enum<?>> setDefaultFilters() {
        // the default filters map is empty - with no filters set
        return new HashMap<>();
    }

    @GetMapping("/filters")
    String updateFilters(@ModelAttribute("usersFilters") Map<String, Enum<?>> filters,
                         @RequestParam(value = "enabled", required = false) User.Enabled enabledParam,
                         @RequestParam(value = "authority", required = false) User.Authority authorityParam,
                         @RequestParam(value = "orderBy", required = false) User.OrderBy orderByParam) {
        filters.put("enabled", enabledParam);
        filters.put("authority", authorityParam);
        filters.put("orderBy", orderByParam);
        return "redirect:/users";
    }

    @GetMapping
    public String showAllUsers(Model model, @ModelAttribute("usersFilters") Map<String, Enum<?>> filters) {
        User.Enabled enabled = (User.Enabled) filters.get("enabled");
        User.Authority authority = (User.Authority) filters.get("authority");
        User.OrderBy orderBy = (User.OrderBy) filters.get("orderBy");
        List<User> usersList = usersRepo.findByEnabledAndAuthorityOrderBy(enabled, authority, orderBy);
        model.addAttribute("usersList", usersList);
        model.addAttribute("usersForm", new UsersForm());
        return "users";
    }

    @PostMapping("/delete")
    public String deleteUser(@ModelAttribute @Valid UsersForm form, BindingResult errors) {
        if (!errors.hasErrors()) {
            String username = form.getUsername();
            try {
                usersRepo.deleteByUsername(username);
                // make all active sessions for deleted user expired
                expireAllSessionsFor(username);
                return "redirect:/users";
            } catch (DataIntegrityViolationException ex) {
                errors.rejectValue("username", null, ex.getMessage());
            }
        }
        return "users";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute @Valid UsersForm form, BindingResult errors) {
        if (!errors.hasErrors()) {
            String username = form.getUsername();
            User.Enabled enabled = form.getEnabled();
            User.Authority authority = form.getAuthority();
            usersRepo.updateEnabledAndAuthorityByUsername(username, enabled, authority);
            // force re-login after user's status and authority have been updated
            expireAllSessionsFor(username);
            return "redirect:/users";
        }
        return "users";
    }

    // invalidates all active sessions for a given user
    private void expireAllSessionsFor(String username) {
        List<Object> loggedUsers = sessionRegistry.getAllPrincipals();
        loggedUsers.stream()
                .filter(user -> ((UserDetails) user).getUsername().equals(username))
                .map(user -> sessionRegistry.getAllSessions(user, false))
                .flatMap(List::stream)
                .forEach(SessionInformation::expireNow);
    }

}
