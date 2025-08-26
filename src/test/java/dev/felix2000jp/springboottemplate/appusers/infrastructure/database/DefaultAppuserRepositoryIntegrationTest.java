package dev.felix2000jp.springboottemplate.appusers.infrastructure.database;

import dev.felix2000jp.springboottemplate.TestcontainersConfiguration;
import dev.felix2000jp.springboottemplate.appusers.domain.Appuser;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.Password;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, DefaultAppuserRepository.class})
class DefaultAppuserRepositoryIntegrationTest {

    @Autowired
    private DefaultAppuserRepository appuserRepository;

    private Appuser appuser;

    @BeforeEach
    void setUp() {
        appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );

        appuserRepository.deleteAll();
        appuserRepository.save(appuser);
    }

    @Test
    void findById_given_id_of_appuser_then_return_appuser() {
        var idValueObject = appuser.getId();
        var actual = appuserRepository.findById(idValueObject);

        assertThat(actual).isPresent();
    }

    @Test
    void findById_given_not_found_id_then_return_empty_optional() {
        var idValueObject = new AppuserId(UUID.randomUUID());
        var actual = appuserRepository.findById(idValueObject);

        assertThat(actual).isNotPresent();
    }

    @Test
    void findByUsername_given_username_of_appuser_then_return_appuser() {
        var usernameValueObject = appuser.getUsername();
        var actual = appuserRepository.findByUsername(usernameValueObject);

        assertThat(actual).isPresent();
    }

    @Test
    void findByUsername_given_not_found_username_then_return_empty_optional() {
        var usernameValueObject = new Username("non existent username");
        var actual = appuserRepository.findByUsername(usernameValueObject);

        assertThat(actual).isNotPresent();
    }

    @Test
    void existsByUsername_given_username_of_appuser_then_return_true() {
        var usernameValueObject = appuser.getUsername();
        var actual = appuserRepository.existsByUsername(usernameValueObject);

        assertThat(actual).isTrue();
    }

    @Test
    void existsByUsername_given_not_found_username_then_return_false() {
        var usernameValueObject = new Username("non existent username");
        var actual = appuserRepository.existsByUsername(usernameValueObject);

        assertThat(actual).isFalse();
    }

    @Test
    void delete_given_appuser_then_delete_appuser() {
        appuserRepository.delete(appuser);

        var idValueObject = appuser.getId();
        var deletedAppuser = appuserRepository.findById(idValueObject);
        assertThat(deletedAppuser).isNotPresent();
    }

    @Test
    void deleteAll_then_delete_all_appusers() {
        appuserRepository.deleteAll();

        var idValueObject = appuser.getId();
        var deletedAppuser = appuserRepository.findById(idValueObject);
        assertThat(deletedAppuser).isNotPresent();
    }

    @Test
    void save_given_appuser_then_save_appuser() {
        var appuserToCreate = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );

        appuserRepository.save(appuserToCreate);

        var idValueObject = appuserToCreate.getId();
        var createdAppuser = appuserRepository.findById(idValueObject);
        assertThat(createdAppuser).isPresent();
    }

}
