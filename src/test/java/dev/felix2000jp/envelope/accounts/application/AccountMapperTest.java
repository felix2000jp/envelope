package dev.felix2000jp.envelope.accounts.application;

import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.Transaction;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper accountMapper = new AccountMapper();

    @Test
    void toAccountDto_given_account_then_map_to_Account_dto() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(250.75))
        );

        var actual = accountMapper.toAccountDto(account);

        assertThat(actual.id()).isEqualTo(account.getId().value());
        assertThat(actual.name()).isEqualTo(account.getName().value());
        assertThat(actual.balance()).isEqualTo(account.getBalance().value());
        assertThat(actual.closed()).isEqualTo(account.isClosed());
    }

    @Test
    void toTransactionDto_given_transaction_then_map_to_Transaction_dto() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(150.25)),
                new TransactionDate(LocalDate.of(2024, 12, 15)),
                new TransactionMemo("Test transaction"),
                true
        );

        var actual = accountMapper.toTransactionDto(transaction);

        assertThat(actual.id()).isEqualTo(transaction.getId().value());
        assertThat(actual.amount()).isEqualTo(transaction.getAmount().value());
        assertThat(actual.dateOfTransaction()).isEqualTo(transaction.getDateOfTransaction().value());
        assertThat(actual.memo()).isEqualTo(transaction.getMemo().value());
        assertThat(actual.cleared()).isEqualTo(transaction.isCleared());
    }

}