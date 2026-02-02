package dev.felix2000jp.springboottemplate.appusers.infrastructure.api;

import dev.felix2000jp.springboottemplate.TestcontainersConfiguration;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.AppuserDto;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.CreateAppuserDto;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.UpdateAppuserDto;
import dev.felix2000jp.springboottemplate.appusers.domain.Appuser;
import dev.felix2000jp.springboottemplate.appusers.domain.AppuserRepository;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.Password;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.Username;
import dev.felix2000jp.springboottemplate.system.security.SecurityScope;
import dev.felix2000jp.springboottemplate.system.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import({TestcontainersConfiguration.class})
class AppuserControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private AppuserRepository appuserRepository;

    private Appuser appuser;
    private String token;

    @BeforeEach
    void setUp() {
        appuserRepository.deleteAll();

        appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password(securityService.generateEncodedPassword("password"))
        );
        appuser.addApplicationScope();

        appuserRepository.save(appuser);

        token = securityService.generateToken(
                appuser.getId().value(),
                appuser.getUsername().value(),
                List.of(SecurityScope.APPLICATION)
        );
    }

    @Test
    void get_then_return_appuser() {
        var getAppuserEntity = restTestClient
                .get()
                .uri("/api/appusers")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AppuserDto.class)
                .returnResult();

        assertThat(getAppuserEntity.getResponseBody()).isNotNull();
    }

    @Test
    void create_then_create_appuser() {
        restTestClient
                .post()
                .uri("/api/appusers")
                .headers(h -> h.setBearerAuth(token))
                .body(new CreateAppuserDto("new username", "password"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Void.class);

        var createdAppuser = appuserRepository.findByUsername(new Username("new username"));
        assertThat(createdAppuser).isPresent();
    }

    @Test
    void update_then_update_appuser() {
        restTestClient
                .put()
                .uri("/api/appusers")
                .headers(h -> h.setBearerAuth(token))
                .body(new UpdateAppuserDto("updated username", "updated password"))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        var updatedAppuser = appuserRepository.findById(appuser.getId());
        assertThat(updatedAppuser).isPresent();
        assertThat(updatedAppuser.get().getUsername().value()).isEqualTo("updated username");
    }

    @Test
    void delete_then_delete_appuser() {
        restTestClient
                .delete()
                .uri("/api/appusers")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        var deletedAppuser = appuserRepository.findById(appuser.getId());
        assertThat(deletedAppuser).isNotPresent();
    }

    @Test
    void login_given_credentials_then_return_token() {
        var loginTokenEntity = restTestClient
                .post()
                .uri("/api/appusers/login")
                .headers(h -> h.setBasicAuth("username", "password"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult();

        assertThat(loginTokenEntity.getResponseBody()).isNotNull();
        assertThat(loginTokenEntity.getResponseBody()).isNotBlank();
    }
}
