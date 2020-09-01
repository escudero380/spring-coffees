package springcoffees.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebSecurityAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse resp,
                                        AuthenticationException e) throws IOException {
        String message = (e instanceof BadCredentialsException)
                ? "Bad credentials. Check your username and password!"
                : (e instanceof DisabledException)
                        ? "Sorry, this user is currently disabled!"
                        : "Failed to login.";
        req.getSession().setAttribute("LAST_AUTH_FAILURE_MESSAGE", message);
        resp.sendRedirect("/login?error");
    }
}
