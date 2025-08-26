package dev.felix2000jp.envelope.appusers.domain;

import dev.felix2000jp.envelope.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Password;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Scope;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppuserTest {

    @Test
    void from_given_valid_parameters_then_create_appuser() {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );

        assertThat(appuser.getUsername().value()).isEqualTo("username");
        assertThat(appuser.getPassword().value()).isEqualTo("password");
        assertThat(appuser.getScopes()).isEmpty();
    }

    @Test
    void setUsername_given_valid_username_then_set_username() {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );
        var newUsername = new Username("new-username");

        appuser.setUsername(newUsername);

        assertThat(appuser.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void setPassword_given_valid_password_then_set_password() {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );
        var newPassword = new Password("new-password");

        appuser.setPassword(newPassword);

        assertThat(appuser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void addApplicationScope_then_add_application_scope() {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );
        appuser.addApplicationScope();

        assertThat(appuser.getScopes()).hasSize(1);
        assertThat(appuser.getScopes()).contains(new Scope("APPLICATION"));
    }

    @Test
    void delete_then_add_delete_event_to_domain_events() {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );

        appuser.delete();

        assertThat(appuser.getDomainEvents()).hasSize(1);
    }

}
