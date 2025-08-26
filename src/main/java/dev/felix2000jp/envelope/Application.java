package dev.felix2000jp.envelope;

import org.springframework.boot.SpringApplication;
import org.springframework.modulith.Modulith;

@Modulith(systemName = "Envelope", sharedModules = "system")
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
