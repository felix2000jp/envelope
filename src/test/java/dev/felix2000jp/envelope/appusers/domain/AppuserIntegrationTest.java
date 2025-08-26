package dev.felix2000jp.envelope.appusers.domain;

import dev.felix2000jp.envelope.TestcontainersConfiguration;
import dev.felix2000jp.envelope.appusers.domain.events.AppuserDeletedEvent;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Password;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
@Import({TestcontainersConfiguration.class})
class AppuserIntegrationTest {

    @Autowired
    AppuserRepository appuserRepository;

    @Test
    void delete_given_transaction_then_publish_appuser_deleted_event(Scenario scenario) {
        var appuser = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username("username"),
                new Password("password")
        );
        appuser.delete();

        scenario
                .stimulate(() -> appuserRepository.save(appuser))
                .andWaitForEventOfType(AppuserDeletedEvent.class)
                .toArriveAndVerify(event -> assertThat(event.appuserId()).isEqualTo(appuser.getId().value()));

        assertThat(appuser.getDomainEvents()).isEmpty();
    }

}
