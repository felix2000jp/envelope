package dev.felix2000jp.envelope;

import org.springframework.boot.SpringApplication;
import org.springframework.modulith.Modulith;

@Modulith(systemName = "envelope", sharedModules = "system")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
