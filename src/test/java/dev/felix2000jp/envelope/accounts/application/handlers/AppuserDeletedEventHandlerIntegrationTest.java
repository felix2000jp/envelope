package dev.felix2000jp.envelope.accounts.application.handlers;

import dev.felix2000jp.envelope.TestcontainersConfiguration;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountName;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import dev.felix2000jp.envelope.appusers.domain.events.AppuserDeletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
@Import({TestcontainersConfiguration.class})
class AppuserDeletedEventHandlerIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void on_then_delete_all_accounts_for_specific_user_only(Scenario scenario) {
        var user1 = new UserId(UUID.randomUUID());
        accountRepository.save(Account.from(new AccountId(UUID.randomUUID()), user1, new AccountName("Checking")));
        accountRepository.save(Account.from(new AccountId(UUID.randomUUID()), user1, new AccountName("Investment")));

        var user2 = new UserId(UUID.randomUUID());
        accountRepository.save(Account.from(new AccountId(UUID.randomUUID()), user2, new AccountName("Checking")));
        accountRepository.save(Account.from(new AccountId(UUID.randomUUID()), user2, new AccountName("Investment")));

        var appuserDeletedEvent = new AppuserDeletedEvent(user1.value());
        scenario
                .publish(appuserDeletedEvent)
                .andWaitForStateChange(() -> accountRepository.findAllByUserId(user1).size())
                .andVerify(userAccountCount -> {
                    var user2Accounts = accountRepository.findAllByUserId(user2);

                    assertThat(userAccountCount).isZero();
                    assertThat(user2Accounts).hasSize(2);
                });
    }

}