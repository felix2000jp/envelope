package dev.felix2000jp.envelope.accounts.domain;

import dev.felix2000jp.envelope.accounts.domain.exceptions.TransactionNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void from_given_valid_parameters_with_balance_then_create_account() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );

        assertThat(account.getName().value()).isEqualTo("Test Account");
        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(account.isClosed()).isFalse();
        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getTransactions().getFirst().getAmount().value()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(account.getTransactions().getFirst().isCleared()).isTrue();
    }

    @Test
    void from_given_valid_parameters_without_balance_then_create_account_with_zero_balance() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );

        assertThat(account.getName().value()).isEqualTo("Test Account");
        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.ZERO);
        assertThat(account.isClosed()).isFalse();
        assertThat(account.getTransactions()).isEmpty();
    }

    @Test
    void setName_given_valid_name_then_update_name() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Original Name")
        );

        var newName = new AccountName("Updated Name");
        account.setName(newName);

        assertThat(account.getName().value()).isEqualTo("Updated Name");
    }

    @Test
    void setBalance_given_valid_balance_then_update_balance() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );

        var newBalance = new AccountBalance(BigDecimal.valueOf(250.50));
        account.setBalance(newBalance);

        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(250.50));
        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getTransactions().getFirst().getAmount().value()).isEqualTo(BigDecimal.valueOf(250.50));
        assertThat(account.getTransactions().getFirst().isCleared()).isTrue();
    }

    @Test
    void setBalance_given_negative_balance_change_then_create_negative_transaction() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(500.00))
        );

        var newBalance = new AccountBalance(BigDecimal.valueOf(200.00));
        account.setBalance(newBalance);

        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(account.getTransactions()).hasSize(2);
        assertThat(account.getTransactions().get(1).getAmount().value()).isEqualTo(BigDecimal.valueOf(-300.00));
        assertThat(account.getTransactions().get(1).isCleared()).isTrue();
    }

    @Test
    void close_given_open_account_then_close_account() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );

        account.close();

        assertThat(account.isClosed()).isTrue();
    }

    @Test
    void open_given_closed_account_then_open_account() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );

        account.close();
        account.open();

        assertThat(account.isClosed()).isFalse();
    }

    @Test
    void addTransaction_given_uncleared_transaction_then_transaction_added_without_balance_update() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );

        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionMemo("Test transaction"),
                false
        );

        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(account.getTransactions()).hasSize(2);
        assertThat(account.getTransactions().get(1).getAmount().value()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(account.getTransactions().get(1).getMemo().value()).isEqualTo("Test transaction");
        assertThat(account.getTransactions().get(1).isCleared()).isFalse();
    }

    @Test
    void addTransaction_given_cleared_positive_transaction_then_balance_increased() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );

        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(250.75)),
                new TransactionMemo("Test transaction"),
                true
        );

        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(1250.75));
        assertThat(account.getTransactions()).hasSize(2);
        assertThat(account.getTransactions().get(1).getAmount().value()).isEqualTo(BigDecimal.valueOf(250.75));
        assertThat(account.getTransactions().get(1).getMemo().value()).isEqualTo("Test transaction");
        assertThat(account.getTransactions().get(1).isCleared()).isTrue();
    }

    @Test
    void addTransaction_given_cleared_negative_transaction_then_balance_decreased() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );

        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(-150.25)),
                new TransactionMemo("Withdrawal transaction"),
                true
        );

        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(849.75));
        assertThat(account.getTransactions()).hasSize(2);
        assertThat(account.getTransactions().get(1).getAmount().value()).isEqualTo(BigDecimal.valueOf(-150.25));
        assertThat(account.getTransactions().get(1).getMemo().value()).isEqualTo("Withdrawal transaction");
        assertThat(account.getTransactions().get(1).isCleared()).isTrue();
    }

    @Test
    void updateTransaction_given_valid_parameters_then_update_transaction() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Original memo"),
                false
        );
        var transactionId = account.getTransactions().get(1).getId();
        var newDate = LocalDate.of(2024, 12, 15);

        account.updateTransaction(
                transactionId,
                new TransactionAmount(BigDecimal.valueOf(200.00)),
                new TransactionDate(newDate),
                new TransactionMemo("Updated memo")
        );

        var updatedTransaction = account.getTransactions().get(1);
        assertThat(updatedTransaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(updatedTransaction.getDateOfTransaction().value()).isEqualTo(newDate);
        assertThat(updatedTransaction.getMemo().value()).isEqualTo("Updated memo");
    }

    @Test
    void updateTransaction_given_partial_update_then_update_only_provided_fields() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Original memo"),
                false
        );
        var transactionId = account.getTransactions().get(1).getId();
        var originalDate = account.getTransactions().get(1).getDateOfTransaction();

        account.updateTransaction(
                transactionId,
                new TransactionAmount(BigDecimal.valueOf(300.00)),
                null,
                new TransactionMemo("Updated memo only")
        );

        var updatedTransaction = account.getTransactions().get(1);
        assertThat(updatedTransaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(300.00));
        assertThat(updatedTransaction.getDateOfTransaction()).isEqualTo(originalDate);
        assertThat(updatedTransaction.getMemo().value()).isEqualTo("Updated memo only");
        assertThat(updatedTransaction.isCleared()).isFalse();
    }

    @Test
    void updateTransaction_given_nonexistent_transaction_id_then_throw_exception() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );

        var nonExistentId = new TransactionId(UUID.randomUUID());
        var updatedAmount = new TransactionAmount(BigDecimal.valueOf(100.00));
        var updatedDate = new TransactionDate(LocalDate.now());
        var updatedMemo = new TransactionMemo("Test");

        assertThatThrownBy(
                () -> account.updateTransaction(nonExistentId, updatedAmount, updatedDate, updatedMemo)
        ).isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void removeTransaction_given_uncleared_transaction_then_remove_without_balance_update() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionMemo("Test transaction"),
                false
        );
        var transactionId = account.getTransactions().get(1).getId();
        var initialBalance = account.getBalance().value();

        account.removeTransaction(transactionId);

        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getBalance().value()).isEqualTo(initialBalance);
    }

    @Test
    void removeTransaction_given_cleared_transaction_then_remove_and_update_balance() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(200.00)),
                new TransactionMemo("Cleared transaction"),
                true
        );
        var transactionId = account.getTransactions().get(1).getId();

        account.removeTransaction(transactionId);

        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getBalance().value()).isEqualTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    void removeTransaction_given_nonexistent_transaction_id_then_throw_exception() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );
        var nonExistentId = new TransactionId(UUID.randomUUID());

        assertThatThrownBy(() -> account.removeTransaction(nonExistentId)).isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void clearTransaction_given_uncleared_transaction_then_clear_and_update_balance() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(200.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Uncleared transaction"),
                false
        );
        var transactionId = account.getTransactions().get(1).getId();
        var initialBalance = account.getBalance().value();

        account.clearTransaction(transactionId);

        assertThat(account.getTransactions().get(1).isCleared()).isTrue();
        assertThat(account.getBalance().value()).isEqualTo(initialBalance.add(BigDecimal.valueOf(200.00)));
    }

    @Test
    void clearTransaction_given_already_cleared_transaction_then_no_change() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(150.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Already cleared transaction"),
                true
        );
        var transactionId = account.getTransactions().get(1).getId();
        var initialBalance = account.getBalance().value();

        account.clearTransaction(transactionId);

        assertThat(account.getTransactions().get(1).isCleared()).isTrue();
        assertThat(account.getBalance().value()).isEqualTo(initialBalance);
    }

    @Test
    void clearTransaction_given_nonexistent_transaction_id_then_throw_exception() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );
        var nonExistentId = new TransactionId(UUID.randomUUID());

        assertThatThrownBy(() -> account.clearTransaction(nonExistentId))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void unclearTransaction_given_cleared_transaction_then_unclear_and_update_balance() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(300.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Cleared transaction"),
                true
        );
        var transactionId = account.getTransactions().get(1).getId();
        var initialBalance = account.getBalance().value();

        account.unclearTransaction(transactionId);

        assertThat(account.getTransactions().get(1).isCleared()).isFalse();
        assertThat(account.getBalance().value()).isEqualTo(initialBalance.subtract(BigDecimal.valueOf(300.00)));
    }

    @Test
    void unclearTransaction_given_already_uncleared_transaction_then_no_change() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(1000.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Already uncleared transaction"),
                false
        );
        var transactionId = account.getTransactions().get(1).getId();
        var initialBalance = account.getBalance().value();

        account.unclearTransaction(transactionId);

        assertThat(account.getTransactions().get(1).isCleared()).isFalse();
        assertThat(account.getBalance().value()).isEqualTo(initialBalance);
    }

    @Test
    void unclearTransaction_given_nonexistent_transaction_id_then_throw_exception() {
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account")
        );
        var nonExistentId = new TransactionId(UUID.randomUUID());

        assertThatThrownBy(() -> account.unclearTransaction(nonExistentId))
                .isInstanceOf(TransactionNotFoundException.class);
    }

}