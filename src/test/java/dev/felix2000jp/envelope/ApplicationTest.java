package dev.felix2000jp.envelope;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
@Import({TestcontainersConfiguration.class})
class ApplicationTest {

    @Test
    void contextLoads() {
        ApplicationModules.of(Application.class).verify();
    }

}
