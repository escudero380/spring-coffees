package springcoffees.web;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
    }

    // an application context delegates the message resolution to
    // a bean with exact "messageSource" name - don't change it!
    @Bean("messageSource")
    public MessageSource setMessageSourceForDefaultMessagesResolution() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:templates/error/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}

