package springcoffees.security;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/restart")
@Profile("demo")
public class RestartController {

    private final Flyway flyway;

    @Autowired
    public RestartController(Flyway flyway) {
        this.flyway = flyway;
    }

    @GetMapping
    public String showNotice() {
        return "restart";
    }

    @PostMapping
    public String reloadTables(HttpSession session) {
        flyway.clean();
        flyway.migrate();
        session.invalidate();
        return "redirect:/";
    }

}
