package springcoffees.app;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class SpringCoffeesApplicationTests {

    @Test
    void contextLoads() {
        // because we used @SpringBootTest annotation, it automatically carries out an integration test
        // by loading the application context and verifying no errors occur during beans instantiation
    }

}
