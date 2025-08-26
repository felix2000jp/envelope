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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class})
class AppuserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private AppuserRepository appuserRepository;

    private Appuser appuser;
    private HttpHeaders headers;

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

        var token = securityService.generateToken(
                appuser.getId().value(),
                appuser.getUsername().value(),
                List.of(SecurityScope.APPLICATION)
        );
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
    }

    @Test
    void get_then_return_appuser() {
        var getAppuserEntity = testRestTemplate.exchange(
                "/api/appusers",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AppuserDto.class
        );

        assertThat(getAppuserEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(getAppuserEntity.getBody()).isNotNull();
    }

    @Test
    void create_then_create_appuser() {
        var createAppuserEntity = testRestTemplate.exchange(
                "/api/appusers",
                HttpMethod.POST,
                new HttpEntity<>(new CreateAppuserDto("new username", "password"), null),
                Void.class
        );

        assertThat(createAppuserEntity.getStatusCode().value()).isEqualTo(201);

        var createdAppuser = appuserRepository.findByUsername(new Username("new username"));
        assertThat(createdAppuser).isPresent();
    }

    @Test
    void update_then_update_appuser() {
        var updatePasswordEntity = testRestTemplate.exchange(
                "/api/appusers",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateAppuserDto("updated username", "updated password"), headers),
                Void.class
        );

        assertThat(updatePasswordEntity.getStatusCode().value()).isEqualTo(204);

        var updatedAppuser = appuserRepository.findById(appuser.getId());
        assertThat(updatedAppuser).isPresent();
        assertThat(updatedAppuser.get().getUsername().value()).isEqualTo("updated username");
    }

    @Test
    void delete_then_delete_appuser() {
        var deleteAppuserEntity = testRestTemplate.exchange(
                "/api/appusers",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(deleteAppuserEntity.getStatusCode().value()).isEqualTo(204);

        var deletedAppuser = appuserRepository.findById(appuser.getId());
        assertThat(deletedAppuser).isNotPresent();
    }

    @Test
    void login_given_credentials_then_return_token() {
        var loginTokenEntity = testRestTemplate.withBasicAuth("username", "password").exchange(
                "/api/appusers/login",
                HttpMethod.POST,
                new HttpEntity<>(null),
                String.class
        );

        assertThat(loginTokenEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(loginTokenEntity.getBody()).isNotBlank();
    }
}
