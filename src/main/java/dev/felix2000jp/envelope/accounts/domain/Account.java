package dev.felix2000jp.envelope.accounts.domain;

import dev.felix2000jp.envelope.accounts.domain.exceptions.TransactionNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.*;
import jakarta.persistence.*;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@jakarta.persistence.Table(name = "account")
@jakarta.persistence.Entity
public class Account implements AggregateRoot<Account, AccountId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private AccountId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "userId", nullable = false))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private AccountName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "balance", nullable = false))
    private AccountBalance balance;

    @Column(name = "closed", nullable = false)
    private boolean closed;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_id", nullable = false)
    private List<Transaction> transactions;

    @Transient
    private final Collection<DomainEvent> domainEvents = new ArrayList<>();

    protected Account() {
    }

    protected Account(AccountId id, UserId userId, AccountName name, AccountBalance balance) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.closed = false;
        this.transactions = new ArrayList<>();
    }

    public static Account from(AccountId id, UserId userId, AccountName name, AccountBalance balance) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(name, "name must not be null");
        Assert.notNull(balance, "balance must not be null");

        var account = new Account(id, userId, name, balance);

        if (balance.value().compareTo(BigDecimal.ZERO) != 0) {
            var initialTransaction = Transaction.from(
                    new TransactionId(UUID.randomUUID()),
                    new TransactionAmount(balance.value()),
                    true
            );
            account.transactions.add(initialTransaction);
        }

        return account;
    }

    public static Account from(AccountId id, UserId userId, AccountName name) {
        return from(id, userId, name, new AccountBalance(BigDecimal.ZERO));
    }

    @Override
    public AccountId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public AccountName getName() {
        return name;
    }

    public AccountBalance getBalance() {
        return balance;
    }

    public boolean isClosed() {
        return closed;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setName(AccountName name) {
        Assert.notNull(name, "name must not be null");

        this.name = name;
    }

    public void setBalance(AccountBalance balance) {
        Assert.notNull(balance, "balance must not be null");

        var balanceDifference = balance.value().subtract(this.balance.value());
        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                new TransactionAmount(balanceDifference),
                new TransactionDate(LocalDate.now()),
                new TransactionMemo(""),
                true
        );

        this.transactions.add(transaction);
        this.balance = balance;
    }

    public void close() {
        this.closed = true;
    }

    public void open() {
        this.closed = false;
    }

    public void addTransaction(TransactionAmount amount, TransactionDate date, TransactionMemo memo, boolean cleared) {
        Assert.notNull(amount, "amount must not be null");
        Assert.notNull(date, "date must not be null");

        var transaction = Transaction.from(
                new TransactionId(UUID.randomUUID()),
                amount,
                date,
                memo,
                cleared
        );
        this.transactions.add(transaction);

        if (transaction.isCleared()) {
            this.balance = new AccountBalance(this.balance.value().add(transaction.getAmount().value()));
        }
    }

    public void addTransaction(TransactionAmount amount, TransactionMemo memo, boolean cleared) {
        addTransaction(amount, new TransactionDate(LocalDate.now()), memo, cleared);
    }

    public void updateTransaction(TransactionId transactionId, TransactionAmount amount, TransactionDate date, TransactionMemo memo) {
        Assert.notNull(transactionId, "transactionId must not be null");

        var transaction = this.transactions.stream()
                .filter(tr -> tr.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(TransactionNotFoundException::new);

        if (amount != null) {
            transaction.setAmount(amount);
        }

        if (date != null) {
            transaction.setDateOfTransaction(date);
        }

        if (memo != null) {
            transaction.setMemo(memo);
        }

        if (transaction.isCleared()) {
            this.balance = new AccountBalance(this.balance.value().add(transaction.getAmount().value()));
        }
    }

    public void removeTransaction(TransactionId transactionId) {
        Assert.notNull(transactionId, "transactionId must not be null");

        var transaction = this.transactions.stream()
                .filter(tr -> tr.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.isCleared()) {
            this.balance = new AccountBalance(this.balance.value().subtract(transaction.getAmount().value()));
        }

        this.transactions.remove(transaction);
    }

    public void clearTransaction(TransactionId transactionId) {
        Assert.notNull(transactionId, "transactionId must not be null");

        var transaction = this.transactions.stream()
                .filter(tr -> tr.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(TransactionNotFoundException::new);

        if (!transaction.isCleared()) {
            transaction.clear();
            this.balance = new AccountBalance(this.balance.value().add(transaction.getAmount().value()));
        }
    }

    public void unclearTransaction(TransactionId transactionId) {
        Assert.notNull(transactionId, "transactionId must not be null");

        var transaction = this.transactions.stream()
                .filter(tr -> tr.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.isCleared()) {
            transaction.unclear();
            this.balance = new AccountBalance(this.balance.value().subtract(transaction.getAmount().value()));
        }
    }

    @DomainEvents
    Collection<DomainEvent> getDomainEvents() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    void clearDomainEvents() {
        domainEvents.clear();
    }

}
