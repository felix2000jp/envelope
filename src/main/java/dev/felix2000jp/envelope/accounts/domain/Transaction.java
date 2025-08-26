package dev.felix2000jp.envelope.accounts.domain;

import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionAmount;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionDate;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionMemo;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import org.jmolecules.ddd.types.Entity;
import org.springframework.util.Assert;

import java.time.LocalDate;

@jakarta.persistence.Table(name = "transaction")
@jakarta.persistence.Entity
public class Transaction implements Entity<Account, TransactionId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private TransactionId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "amount", nullable = false))
    private TransactionAmount amount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "date_of_transaction", nullable = false))
    private TransactionDate dateOfTransaction;

    @AttributeOverride(name = "value", column = @Column(name = "memo", nullable = false))
    private TransactionMemo memo;

    @Column(name = "cleared", nullable = false)
    private boolean cleared;

    protected Transaction() {
    }

    protected Transaction(
            TransactionId id,
            TransactionAmount amount,
            TransactionDate dateOfTransaction,
            TransactionMemo memo,
            boolean cleared
    ) {
        this.id = id;
        this.amount = amount;
        this.dateOfTransaction = dateOfTransaction;
        this.memo = memo;
        this.cleared = cleared;
    }

    public static Transaction from(
            TransactionId id,
            TransactionAmount amount,
            TransactionDate dateOfTransaction,
            TransactionMemo memo,
            boolean cleared
    ) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(amount, "amount must not be null");
        Assert.notNull(dateOfTransaction, "dateOfTransaction must not be null");
        Assert.notNull(memo, "memo must not be null");

        return new Transaction(id, amount, dateOfTransaction, memo, cleared);
    }

    public static Transaction from(
            TransactionId id,
            TransactionAmount amount,
            boolean cleared
    ) {
        return from(id, amount, new TransactionDate(LocalDate.now()), new TransactionMemo(""), cleared);
    }

    @Override
    public TransactionId getId() {
        return id;
    }

    public TransactionAmount getAmount() {
        return amount;
    }

    public TransactionDate getDateOfTransaction() {
        return dateOfTransaction;
    }

    public TransactionMemo getMemo() {
        return memo;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setAmount(TransactionAmount amount) {
        Assert.notNull(amount, "amount must not be null");

        this.amount = amount;
    }

    public void setDateOfTransaction(TransactionDate dateOfTransaction) {
        Assert.notNull(dateOfTransaction, "dateOfTransaction must not be null");

        this.dateOfTransaction = dateOfTransaction;
    }

    public void setMemo(TransactionMemo memo) {
        this.memo = memo;
    }

    public void clear() {
        this.cleared = true;
    }

    public void unclear() {
        this.cleared = false;
    }

}
