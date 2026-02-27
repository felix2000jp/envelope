package dev.felix2000jp.envelope.appusers.application;

import dev.felix2000jp.envelope.appusers.domain.Appuser;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Password;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppuserMapperTest {

    private final AppuserMapper appuserMapper = new AppuserMapper();

    @Test
    void toDto_given_appuser_then_map_to_dto() {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );
        appuser.addApplicationScope();

        var actual = appuserMapper.toDto(appuser);

        assertThat(actual.id()).isEqualTo(appuser.getId().value());
        assertThat(actual.username()).isEqualTo(appuser.getUsername().value());
        assertThat(actual.scopes()).contains("APPLICATION");
    }

}
