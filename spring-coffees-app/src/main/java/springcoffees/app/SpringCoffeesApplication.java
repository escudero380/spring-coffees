package springcoffees.app;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"springcoffees.jdbc", "springcoffees.security", "springcoffees.web"})
public class SpringCoffeesApplication {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(SpringCoffeesApplication.class, args);
    }

    /* we may invoke this method should we need
       programmatically restart our application */

    @Deprecated
    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);
        // note: we recreate the context in a separate non-daemon thread;
        // this way we prevent the JVM shutdown, triggered by the close()
        // method, from closing our application, so that it wouldn't stop
        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(SpringCoffeesApplication.class, args.getSourceArgs());
        });
        thread.setDaemon(false);
        thread.start();
    }

}