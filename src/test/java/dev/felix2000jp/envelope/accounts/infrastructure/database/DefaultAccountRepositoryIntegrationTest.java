package dev.felix2000jp.envelope.accounts.infrastructure.database;

import dev.felix2000jp.envelope.TestcontainersConfiguration;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, DefaultAccountRepository.class})
class DefaultAccountRepositoryIntegrationTest {

    @Autowired
    private DefaultAccountRepository accountRepository;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(0))
        );

        account.addTransaction(new TransactionAmount(BigDecimal.valueOf(100.00)), new TransactionMemo("Test transaction 1"), true);
        account.addTransaction(new TransactionAmount(BigDecimal.valueOf(200.00)), new TransactionMemo("Test transaction 2"), true);
        account.addTransaction(new TransactionAmount(BigDecimal.valueOf(-50.00)), new TransactionMemo("Test transaction 3"), false);

        accountRepository.deleteAll();
        accountRepository.save(account);
    }

    @Test
    void findAllByUserId_given_userId_of_account_then_return_accounts() {
        var actual = accountRepository.findAllByUserId(account.getUserId());

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getId()).isEqualTo(account.getId());
    }

    @Test
    void findAllByUserId_given_not_found_userId_then_return_empty_list() {
        var actual = accountRepository.findAllByUserId(new UserId(UUID.randomUUID()));

        assertThat(actual).isEmpty();
    }

    @Test
    void findByIdAndUserId_given_id_and_userId_of_account_then_return_account() {
        var idValueObject = account.getId();
        var actual = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());

        assertThat(actual).isPresent();
    }

    @Test
    void findByIdAndUserId_given_not_found_id_then_return_empty_optional() {
        var idValueObject = new AccountId(UUID.randomUUID());
        var actual = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());

        assertThat(actual).isNotPresent();
    }

    @Test
    void findByIdAndUserId_given_not_found_userId_then_return_empty_optional() {
        var idValueObject = account.getId();
        var actual = accountRepository.findByIdAndUserId(idValueObject, new UserId(UUID.randomUUID()));

        assertThat(actual).isNotPresent();
    }

    @Test
    void findWithTransactionsByIdAndUserId_given_id_and_userId_of_account_then_return_account() {
        var idValueObject = account.getId();
        var actual = accountRepository.findWithTransactionsByIdAndUserId(idValueObject, account.getUserId());

        assertThat(actual).isPresent();
    }

    @Test
    void findWithTransactionsByIdAndUserId_given_not_found_id_then_return_empty_optional() {
        var idValueObject = new AccountId(UUID.randomUUID());
        var actual = accountRepository.findWithTransactionsByIdAndUserId(idValueObject, account.getUserId());

        assertThat(actual).isNotPresent();
    }

    @Test
    void findWithTransactionsByIdAndUserId_given_not_found_userId_then_return_empty_optional() {
        var idValueObject = account.getId();
        var actual = accountRepository.findWithTransactionsByIdAndUserId(idValueObject, new UserId(UUID.randomUUID()));

        assertThat(actual).isNotPresent();
    }

    @Test
    void delete_given_account_then_delete_account() {
        accountRepository.delete(account);

        var idValueObject = account.getId();
        var deletedAccount = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());
        assertThat(deletedAccount).isNotPresent();
    }

    @Test
    void deleteAll_then_delete_all_accounts() {
        accountRepository.deleteAll();

        var idValueObject = account.getId();
        var deletedAccount = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());
        assertThat(deletedAccount).isNotPresent();
    }

    @Test
    void deleteAllByUserId_given_userId_then_delete_all_accounts_for_user() {
        accountRepository.deleteAllByUserId(account.getUserId());

        var idValueObject = account.getId();
        var deletedAccount = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());
        assertThat(deletedAccount).isNotPresent();
    }

    @Test
    void deleteAllByUserId_given_not_found_userId_then_no_accounts_deleted() {
        accountRepository.deleteAllByUserId(new UserId(UUID.randomUUID()));

        var idValueObject = account.getId();
        var existingAccount = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());
        assertThat(existingAccount).isPresent();
    }

    @Test
    void save_given_account_then_save_account() {
        var accountToCreate = Account.from(
                new AccountId(UUID.randomUUID()),
                account.getUserId(),
                new AccountName("New Account")
        );

        accountRepository.save(accountToCreate);

        var idValueObject = accountToCreate.getId();
        var createdAccount = accountRepository.findByIdAndUserId(idValueObject, account.getUserId());
        assertThat(createdAccount).isPresent();
    }

}