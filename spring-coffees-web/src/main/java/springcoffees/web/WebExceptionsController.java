package springcoffees.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
public class WebExceptionsController {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleRequestParametersTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", "Type mismatch error");
        mav.addObject("message", "Inappropriate value for the request parameter");
        List<String> errors = List.of(String.format("Parameter '%s' cannot accept value '%s'",
                ex.getName(), ex.getValue()));
        mav.addObject("errors", errors);
        mav.setViewName("error/500");
        return mav;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleRequestParametersConstraints(ConstraintViolationException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", "Request parameters error");
        mav.addObject("message", "One or more request " +
                "parameters contain incorrect values:");
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        mav.addObject("errors", errors);
        mav.setViewName("error/500");
        return mav;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityErrors(DataIntegrityViolationException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", "Data integrity error");
        mav.addObject("message", "This request can not be performed " +
                "because of the database integrity restrictions:");
        String err = Objects.requireNonNullElse(ex.getMessage(), "Unresolved");
        List<String> errors = List.of(err);
        mav.addObject("errors", errors);
        mav.setViewName("error/500");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleOtherErrors() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", "An unexpected error");
        mav.addObject("message", "Unable to process the request:");
        List<String> errors = List.of("Contact website administrator for more details");
        mav.addObject("errors", errors);
        mav.setViewName("error/500");
        return mav;
    }

//    private final MessageSource messageSource;
//
//    public WebExceptionsController() {
//        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
//        messageSource.setBasename("classpath:messages/error-mappings");
//        messageSource.setDefaultEncoding("UTF-8");
//        this.messageSource = messageSource;
//    }
//
//    @ExceptionHandler(BindException.class)
//    public ModelAndView handleFormValidationErrors(HttpServletRequest req, BindException ex) {
//        ModelAndView mav = new ModelAndView();
//        mav.addObject("url", req.getRequestURL());
//        mav.addObject("timestamp", LocalDateTime.now());
//        mav.addObject("exception", "Form validation error");
//        mav.addObject("message", "One or more fields " +
//                "are missing or contain incorrect values:");
//        List<String> errors = ex.getAllErrors().stream()
//                .map(err -> messageSource.getMessage(err, Locale.getDefault()))
//                .collect(Collectors.toList());
//        mav.addObject("errors", errors);
//        mav.setViewName("errors");
//        return mav;
//    }

}
