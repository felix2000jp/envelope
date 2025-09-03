package dev.felix2000jp.envelope.accounts.domain;

import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionAmount;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionDate;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionMemo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void from_given_valid_parameters_then_create_transaction() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Test transaction"),
                false
        );

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(transaction.getDateOfTransaction().value()).isEqualTo(LocalDate.now());
        assertThat(transaction.getMemo().value()).isEqualTo("Test transaction");
        assertThat(transaction.isCleared()).isFalse();
    }

    @Test
    void from_given_cleared_transaction_then_create_cleared_transaction() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(-50.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Cleared transaction"),
                true
        );

        assertThat(transaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(-50.00));
        assertThat(transaction.getDateOfTransaction().value()).isEqualTo(LocalDate.now());
        assertThat(transaction.getMemo().value()).isEqualTo("Cleared transaction");
        assertThat(transaction.isCleared()).isTrue();
    }

    @Test
    void setAmount_given_valid_amount_then_update_amount() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Test transaction"),
                false
        );

        transaction.setAmount(new TransactionAmount(BigDecimal.valueOf(250.00)));

        assertThat(transaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(250.00));
    }

    @Test
    void setDateOfTransaction_given_valid_date_then_update_date() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Test transaction"),
                false
        );
        var newDate = LocalDate.of(2024, 12, 15);

        transaction.setDateOfTransaction(new TransactionDate(newDate));

        assertThat(transaction.getDateOfTransaction().value()).isEqualTo(newDate);
    }

    @Test
    void setMemo_given_valid_memo_then_update_memo() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Test transaction"),
                false
        );

        transaction.setMemo(new TransactionMemo("Updated memo"));

        assertThat(transaction.getMemo().value()).isEqualTo("Updated memo");
    }

    @Test
    void clear_given_uncleared_transaction_then_mark_as_cleared() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Test transaction"),
                false
        );

        transaction.clear();

        assertThat(transaction.isCleared()).isTrue();
    }

    @Test
    void unclear_given_cleared_transaction_then_mark_as_uncleared() {
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(BigDecimal.valueOf(100.00)),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo("Test transaction"),
                true
        );

        transaction.unclear();

        assertThat(transaction.isCleared()).isFalse();
    }

}