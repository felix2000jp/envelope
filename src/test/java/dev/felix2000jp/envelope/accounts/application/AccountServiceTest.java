package dev.felix2000jp.envelope.accounts.application;

import dev.felix2000jp.envelope.accounts.application.dtos.AddTransactionDto;
import dev.felix2000jp.envelope.accounts.application.dtos.UpdateAccountDto;
import dev.felix2000jp.envelope.accounts.application.dtos.UpdateTransactionDto;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.exceptions.AccountNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.exceptions.TransactionNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountBalance;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountName;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionAmount;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionDate;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionMemo;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import dev.felix2000jp.envelope.system.security.SecurityService;
import dev.felix2000jp.envelope.system.security.SecurityUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Spy
    private AccountMapper accountMapper;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private AccountService accountService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Test
    void get_then_return_account_list_of_accounts() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var userId = new UserId(securityUser.id());
        var account1 = Account.from(
                new AccountId(UUID.randomUUID()),
                userId,
                new AccountName("Account 1"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        var account2 = Account.from(
                new AccountId(UUID.randomUUID()),
                userId,
                new AccountName("Account 2"),
                new AccountBalance(BigDecimal.valueOf(200.00))
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of(account1, account2));

        var actual = accountService.get();

        assertThat(actual.total()).isEqualTo(2);
        assertThat(actual.accounts().getFirst().id()).isEqualTo(account1.getId().value());
        assertThat(actual.accounts().getFirst().name()).isEqualTo(account1.getName().value());
        assertThat(actual.accounts().getFirst().balance()).isEqualTo(account1.getBalance().value());
        assertThat(actual.accounts().get(1).id()).isEqualTo(account2.getId().value());
        assertThat(actual.accounts().get(1).name()).isEqualTo(account2.getName().value());
        assertThat(actual.accounts().get(1).balance()).isEqualTo(account2.getBalance().value());
    }

    @Test
    void get_given_no_accounts_then_return_empty_list() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var userId = new UserId(securityUser.id());

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of());

        var actual = accountService.get();

        assertThat(actual.total()).isZero();
        assertThat(actual.accounts()).isEmpty();
    }

    @Test
    void create_then_return_created() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var name = "New Account";
        var initialBalance = BigDecimal.valueOf(250.75);

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);

        var actual = accountService.create(name, initialBalance);

        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUserId().value()).isEqualTo(securityUser.id());
        assertThat(accountCaptor.getValue().getName().value()).isEqualTo(name);
        assertThat(accountCaptor.getValue().getBalance().value()).isEqualTo(initialBalance);
        assertThat(accountCaptor.getValue().isClosed()).isFalse();
        assertThat(accountCaptor.getValue().getTransactions()).hasSize(1);

        assertThat(actual.id()).isNotNull();
        assertThat(actual.name()).isEqualTo(name);
        assertThat(actual.balance()).isEqualTo(initialBalance);
        assertThat(actual.closed()).isFalse();
    }

    @Test
    void create_with_zero_balance_then_return_created() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var name = "Zero Account";
        var initialBalance = BigDecimal.ZERO;

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);

        var actual = accountService.create(name, initialBalance);

        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUserId().value()).isEqualTo(securityUser.id());
        assertThat(accountCaptor.getValue().getName().value()).isEqualTo(name);
        assertThat(accountCaptor.getValue().getBalance().value()).isEqualTo(initialBalance);
        assertThat(accountCaptor.getValue().isClosed()).isFalse();
        assertThat(accountCaptor.getValue().getTransactions()).isEmpty();

        assertThat(actual.id()).isNotNull();
        assertThat(actual.name()).isEqualTo(name);
        assertThat(actual.balance()).isEqualTo(initialBalance);
        assertThat(actual.closed()).isFalse();
    }

    @Test
    void update_then_return_updated_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var existingAccount = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Old Name"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        var updateAccountDto = new UpdateAccountDto("Updated Name", BigDecimal.valueOf(500.00));

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(existingAccount));

        var actual = accountService.update(accountId, updateAccountDto);

        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getName().value()).isEqualTo("Updated Name");
        assertThat(accountCaptor.getValue().getBalance().value()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(accountCaptor.getValue().getUserId().value()).isEqualTo(securityUser.id());
        assertThat(accountCaptor.getValue().getId().value()).isEqualTo(accountId);
        assertThat(accountCaptor.getValue().getTransactions()).hasSize(2);

        assertThat(actual.id()).isEqualTo(accountId);
        assertThat(actual.name()).isEqualTo("Updated Name");
        assertThat(actual.balance()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(actual.closed()).isFalse();
    }

    @Test
    void update_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var updateAccountDto = new UpdateAccountDto("Updated Name", BigDecimal.valueOf(500.00));

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.update(accountId, updateAccountDto)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void delete_then_delete_all_accounts_for_user() {
        var userId = UUID.randomUUID();

        accountService.delete(userId);

        verify(accountRepository).deleteAllByUserId(new UserId(userId));
    }

    @Test
    void getAccountById_then_return_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var userId = new UserId(securityUser.id());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                userId,
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(150.50))
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), userId)).thenReturn(Optional.of(account));

        var actual = accountService.getAccountById(accountId);

        assertThat(actual.id()).isEqualTo(account.getId().value());
        assertThat(actual.name()).isEqualTo(account.getName().value());
        assertThat(actual.balance()).isEqualTo(account.getBalance().value());
        assertThat(actual.closed()).isEqualTo(account.isClosed());
    }

    @Test
    void getAccountById_given_not_found_account_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var userId = new UserId(securityUser.id());
        var accountId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), userId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.getAccountById(accountId)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void closeAccount_then_return_closed_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        accountService.closeAccount(accountId);

        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().isClosed()).isTrue();
        assertThat(accountCaptor.getValue().getId().value()).isEqualTo(accountId);
    }

    @Test
    void closeAccount_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.closeAccount(accountId)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void openAccount_then_return_opened_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.close();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        accountService.openAccount(accountId);

        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().isClosed()).isFalse();
        assertThat(accountCaptor.getValue().getId().value()).isEqualTo(accountId);
    }

    @Test
    void openAccount_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.openAccount(accountId)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccountTransactions_then_return_transaction_list() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(50.00)),
                new TransactionDate(LocalDate.of(2024, 1, 15)),
                new TransactionMemo("First transaction"),
                true
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(-25.00)),
                new TransactionDate(LocalDate.of(2024, 2, 10)),
                new TransactionMemo("Second transaction"),
                false
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        var actual = accountService.getAccountTransactions(accountId);

        assertThat(actual.total()).isEqualTo(3);
        assertThat(actual.transactions()).hasSize(3);

        var firstTransaction = actual.transactions().get(1);
        assertThat(firstTransaction.amount()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(firstTransaction.dateOfTransaction()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(firstTransaction.memo()).isEqualTo("First transaction");
        assertThat(firstTransaction.cleared()).isTrue();

        var secondTransaction = actual.transactions().get(2);
        assertThat(secondTransaction.amount()).isEqualTo(BigDecimal.valueOf(-25.00));
        assertThat(secondTransaction.dateOfTransaction()).isEqualTo(LocalDate.of(2024, 2, 10));
        assertThat(secondTransaction.memo()).isEqualTo("Second transaction");
        assertThat(secondTransaction.cleared()).isFalse();
    }

    @Test
    void getAccountTransactions_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.getAccountTransactions(accountId)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void addTransaction_then_return_updated_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        var addTransactionDto = new AddTransactionDto(
                BigDecimal.valueOf(50.00),
                LocalDate.of(2024, 2, 15),
                "Test transaction",
                true
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        var actual = accountService.addTransaction(accountId, addTransactionDto);
        verify(accountRepository).save(accountCaptor.capture());

        var savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getBalance().value()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(savedAccount.getTransactions()).hasSize(2);
        
        var newTransaction = savedAccount.getTransactions().get(1);
        assertThat(newTransaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(newTransaction.getDateOfTransaction().value()).isEqualTo(LocalDate.of(2024, 2, 15));
        assertThat(newTransaction.getMemo().value()).isEqualTo("Test transaction");
        assertThat(newTransaction.isCleared()).isTrue();

        assertThat(actual.id()).isEqualTo(newTransaction.getId().value());
        assertThat(actual.amount()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(actual.dateOfTransaction()).isEqualTo(LocalDate.of(2024, 2, 15));
        assertThat(actual.memo()).isEqualTo("Test transaction");
        assertThat(actual.cleared()).isTrue();
    }

    @Test
    void addTransaction_with_defaults_then_return_updated_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(200.00))
        );
        var addTransactionDto = new AddTransactionDto(
                BigDecimal.valueOf(-25.00),
                null,
                null,
                false
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        var actual = accountService.addTransaction(accountId, addTransactionDto);
        verify(accountRepository).save(accountCaptor.capture());

        var savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getBalance().value()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(savedAccount.getTransactions()).hasSize(2);
        
        var newTransaction = savedAccount.getTransactions().get(1);
        assertThat(newTransaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(-25.00));
        assertThat(newTransaction.getDateOfTransaction().value()).isEqualTo(LocalDate.now());
        assertThat(newTransaction.getMemo().value()).isEmpty();
        assertThat(newTransaction.isCleared()).isFalse();

        assertThat(actual.id()).isEqualTo(newTransaction.getId().value());
        assertThat(actual.amount()).isEqualTo(BigDecimal.valueOf(-25.00));
        assertThat(actual.dateOfTransaction()).isEqualTo(LocalDate.now());
        assertThat(actual.memo()).isEmpty();
        assertThat(actual.cleared()).isFalse();
    }

    @Test
    void addTransaction_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var addTransactionDto = new AddTransactionDto(
                BigDecimal.valueOf(50.00),
                LocalDate.now(),
                "Test transaction",
                true
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.addTransaction(accountId, addTransactionDto)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void updateTransaction_then_return_updated_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(50.00)),
                new TransactionDate(LocalDate.of(2024, 1, 15)),
                new TransactionMemo("Original memo"),
                true
        );
        
        var transactionId = account.getTransactions().get(1).getId().value();
        var updateTransactionDto = new UpdateTransactionDto(
                BigDecimal.valueOf(75.00),
                LocalDate.of(2024, 2, 20),
                "Updated memo"
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        var actual = accountService.updateTransaction(accountId, transactionId, updateTransactionDto);

        verify(accountRepository).save(accountCaptor.capture());
        var savedAccount = accountCaptor.getValue();
        
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getTransactions()).hasSize(2);
        
        var updatedTransaction = savedAccount.getTransactions().get(1);
        assertThat(updatedTransaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(75.00));
        assertThat(updatedTransaction.getDateOfTransaction().value()).isEqualTo(LocalDate.of(2024, 2, 20));
        assertThat(updatedTransaction.getMemo().value()).isEqualTo("Updated memo");

        assertThat(actual.id()).isEqualTo(updatedTransaction.getId().value());
        assertThat(actual.amount()).isEqualTo(BigDecimal.valueOf(75.00));
        assertThat(actual.dateOfTransaction()).isEqualTo(LocalDate.of(2024, 2, 20));
        assertThat(actual.memo()).isEqualTo("Updated memo");
        assertThat(actual.cleared()).isTrue();
    }

    @Test
    void updateTransaction_with_partial_update_then_return_updated_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(50.00)),
                new TransactionDate(LocalDate.of(2024, 1, 15)),
                new TransactionMemo("Original memo"),
                true
        );
        
        var transactionId = account.getTransactions().get(1).getId().value();
        var updateTransactionDto = new UpdateTransactionDto(
                BigDecimal.valueOf(80.00),
                null,
                null
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        var actual = accountService.updateTransaction(accountId, transactionId, updateTransactionDto);

        verify(accountRepository).save(accountCaptor.capture());
        var savedAccount = accountCaptor.getValue();
        
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getTransactions()).hasSize(2);
        
        var updatedTransaction = savedAccount.getTransactions().get(1);
        assertThat(updatedTransaction.getAmount().value()).isEqualTo(BigDecimal.valueOf(80.00));
        assertThat(updatedTransaction.getDateOfTransaction().value()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(updatedTransaction.getMemo().value()).isEqualTo("Original memo");

        assertThat(actual.id()).isEqualTo(updatedTransaction.getId().value());
        assertThat(actual.amount()).isEqualTo(BigDecimal.valueOf(80.00));
        assertThat(actual.dateOfTransaction()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(actual.memo()).isEqualTo("Original memo");
        assertThat(actual.cleared()).isTrue();
    }

    @Test
    void updateTransaction_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var updateTransactionDto = new UpdateTransactionDto(
                BigDecimal.valueOf(75.00),
                LocalDate.of(2024, 2, 20),
                "Updated memo"
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.updateTransaction(accountId, transactionId, updateTransactionDto)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void updateTransaction_given_transaction_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var nonExistentTransactionId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        
        var updateTransactionDto = new UpdateTransactionDto(
                BigDecimal.valueOf(75.00),
                LocalDate.of(2024, 2, 20),
                "Updated memo"
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        assertThatThrownBy(
                () -> accountService.updateTransaction(accountId, nonExistentTransactionId, updateTransactionDto)
        ).isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void removeTransaction_then_return_updated_account() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(50.00)),
                new TransactionDate(LocalDate.of(2024, 1, 15)),
                new TransactionMemo("Transaction to remove"),
                true
        );
        
        var transactionId = account.getTransactions().get(1).getId().value();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        accountService.removeTransaction(accountId, transactionId);

        verify(accountRepository).save(accountCaptor.capture());
        var savedAccount = accountCaptor.getValue();
        
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getTransactions()).hasSize(1);
    }

    @Test
    void removeTransaction_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.removeTransaction(accountId, transactionId)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void removeTransaction_given_transaction_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var nonExistentTransactionId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        assertThatThrownBy(
                () -> accountService.removeTransaction(accountId, nonExistentTransactionId)
        ).isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void clearTransaction_then_clear_transaction() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(50.00)),
                new TransactionDate(LocalDate.of(2024, 1, 15)),
                new TransactionMemo("Transaction to clear"),
                false
        );
        
        var transactionId = account.getTransactions().get(1).getId().value();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        accountService.clearTransaction(accountId, transactionId);

        verify(accountRepository).save(accountCaptor.capture());
        var savedAccount = accountCaptor.getValue();
        
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getTransactions()).hasSize(2);
        
        var clearedTransaction = savedAccount.getTransactions().get(1);
        assertThat(clearedTransaction.isCleared()).isTrue();
    }

    @Test
    void clearTransaction_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> accountService.clearTransaction(accountId, transactionId)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void clearTransaction_given_transaction_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var nonExistentTransactionId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        assertThatThrownBy(
                () -> accountService.clearTransaction(accountId, nonExistentTransactionId)
        ).isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void unclearTransaction_then_unclear_transaction() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );
        account.addTransaction(
                new TransactionAmount(BigDecimal.valueOf(50.00)),
                new TransactionDate(LocalDate.of(2024, 1, 15)),
                new TransactionMemo("Transaction to unclear"),
                true
        );
        
        var transactionId = account.getTransactions().get(1).getId().value();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        accountService.unclearTransaction(accountId, transactionId);

        verify(accountRepository).save(accountCaptor.capture());
        var savedAccount = accountCaptor.getValue();
        
        assertThat(savedAccount.getId().value()).isEqualTo(accountId);
        assertThat(savedAccount.getTransactions()).hasSize(2);
        
        var unclearedTransaction = savedAccount.getTransactions().get(1);
        assertThat(unclearedTransaction.isCleared()).isFalse();
    }

    @Test
    void unclearTransaction_given_account_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.unclearTransaction(accountId, transactionId)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void unclearTransaction_given_transaction_not_found_then_throw_exception() {
        var securityUser = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var accountId = UUID.randomUUID();
        var nonExistentTransactionId = UUID.randomUUID();
        var account = Account.from(
                new AccountId(accountId),
                new UserId(securityUser.id()),
                new AccountName("Test Account"),
                new AccountBalance(BigDecimal.valueOf(100.00))
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(securityUser);
        when(
                accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(securityUser.id()))
        ).thenReturn(Optional.of(account));

        assertThatThrownBy(
                () -> accountService.unclearTransaction(accountId, nonExistentTransactionId)
        ).isInstanceOf(TransactionNotFoundException.class);
    }

}