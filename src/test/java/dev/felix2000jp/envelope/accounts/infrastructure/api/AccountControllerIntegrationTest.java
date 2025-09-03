package dev.felix2000jp.envelope.accounts.infrastructure.api;

import dev.felix2000jp.envelope.TestcontainersConfiguration;
import dev.felix2000jp.envelope.accounts.application.dtos.*;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.*;
import dev.felix2000jp.envelope.system.security.SecurityScope;
import dev.felix2000jp.envelope.system.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class})
class AccountControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private AccountRepository accountRepository;

    private Account account;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(new BigDecimal("1000.00"))
        );

        account.addTransaction(new TransactionAmount(new BigDecimal("100.00")), new TransactionMemo("Test transaction 1"), true);
        account.addTransaction(new TransactionAmount(new BigDecimal("-100.00")), new TransactionMemo("Test transaction 2"), false);

        accountRepository.save(account);
        var token = securityService.generateToken(
                account.getUserId().value(),
                "username",
                List.of(SecurityScope.APPLICATION)
        );
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
    }

    @Test
    void get_then_return_accounts() {
        var getAllAccountsEntity = testRestTemplate.exchange(
                "/api/accounts",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AccountListDto.class
        );

        assertThat(getAllAccountsEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(getAllAccountsEntity.getBody()).isNotNull();
        assertThat(getAllAccountsEntity.getBody().total()).isEqualTo(1);
    }

    @Test
    void create_then_create_account() {
        var createAccountDto = new CreateAccountDto("New Account", new BigDecimal("500.00"));
        var createAccountEntity = testRestTemplate.exchange(
                "/api/accounts",
                HttpMethod.POST,
                new HttpEntity<>(createAccountDto, headers),
                AccountDto.class
        );

        assertThat(createAccountEntity.getStatusCode().value()).isEqualTo(201);
        assertThat(createAccountEntity.getBody()).isNotNull();
        assertThat(createAccountEntity.getBody().name()).isEqualTo("New Account");
        assertThat(createAccountEntity.getBody().balance()).isEqualTo(new BigDecimal("500.00"));
        assertThat(createAccountEntity.getBody().closed()).isFalse();

        var createdAccountId = new AccountId(createAccountEntity.getBody().id());
        var createdAccount = accountRepository.findByIdAndUserId(createdAccountId, account.getUserId());
        assertThat(createdAccount).isPresent();
        assertThat(createdAccount.get().getName().value()).isEqualTo("New Account");
        assertThat(createdAccount.get().getBalance().value()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void update_then_update_account() {
        var updateAccountDto = new UpdateAccountDto("Updated Account Name", new BigDecimal("2000.00"));
        var updateAccountEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value(),
                HttpMethod.PUT,
                new HttpEntity<>(updateAccountDto, headers),
                AccountDto.class
        );

        assertThat(updateAccountEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(updateAccountEntity.getBody()).isNotNull();
        assertThat(updateAccountEntity.getBody().id()).isEqualTo(account.getId().value());
        assertThat(updateAccountEntity.getBody().name()).isEqualTo("Updated Account Name");
        assertThat(updateAccountEntity.getBody().balance()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(updateAccountEntity.getBody().closed()).isFalse();

        var updatedAccount = accountRepository.findByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getName().value()).isEqualTo("Updated Account Name");
        assertThat(updatedAccount.get().getBalance().value()).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    void getAccountById_then_return_account() {
        var getAccountByIdEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AccountDto.class
        );

        assertThat(getAccountByIdEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(getAccountByIdEntity.getBody()).isNotNull();
        assertThat(getAccountByIdEntity.getBody().id()).isEqualTo(account.getId().value());
        assertThat(getAccountByIdEntity.getBody().name()).isEqualTo("Test Account");
        assertThat(getAccountByIdEntity.getBody().balance()).isEqualTo(new BigDecimal("1100.00"));
        assertThat(getAccountByIdEntity.getBody().closed()).isFalse();
    }

    @Test
    void getAccountTransactions_then_return_transaction_list() {
        var getAccountTransactionsEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value() + "/transactions",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TransactionListDto.class
        );

        assertThat(getAccountTransactionsEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(getAccountTransactionsEntity.getBody()).isNotNull();
        assertThat(getAccountTransactionsEntity.getBody().total()).isEqualTo(3);
        assertThat(getAccountTransactionsEntity.getBody().transactions()).hasSize(3);

        var transactions = getAccountTransactionsEntity.getBody().transactions();

        var initialTransaction = transactions.getFirst();
        assertThat(initialTransaction.amount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(initialTransaction.memo()).isEmpty();
        assertThat(initialTransaction.cleared()).isTrue();

        var firstTransaction = transactions.get(1);
        assertThat(firstTransaction.amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(firstTransaction.memo()).isEqualTo("Test transaction 1");
        assertThat(firstTransaction.cleared()).isTrue();

        var secondTransaction = transactions.get(2);
        assertThat(secondTransaction.amount()).isEqualTo(new BigDecimal("-100.00"));
        assertThat(secondTransaction.memo()).isEqualTo("Test transaction 2");
        assertThat(secondTransaction.cleared()).isFalse();
    }

    @Test
    void addTransaction_then_add_transaction_to_account() {
        var addTransactionDto = new AddTransactionDto(
                new BigDecimal("200.00"),
                LocalDate.now(),
                "Test transaction via API",
                true
        );

        var addTransactionEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value() + "/transactions",
                HttpMethod.POST,
                new HttpEntity<>(addTransactionDto, headers),
                TransactionDto.class
        );

        assertThat(addTransactionEntity.getStatusCode().value()).isEqualTo(201);
        assertThat(addTransactionEntity.getBody()).isNotNull();
        assertThat(addTransactionEntity.getBody().amount()).isEqualTo(new BigDecimal("200.00"));

        var updatedAccount = accountRepository.findByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getBalance().value()).isEqualTo(new BigDecimal("1300.00"));
    }

    @Test
    void updateTransaction_then_update_transaction_in_account() {
        var transactionToUpdate = account.getTransactions().get(1);
        var transactionId = transactionToUpdate.getId().value();

        var updateTransactionDto = new UpdateTransactionDto(
                new BigDecimal("250.00"),
                LocalDate.of(2024, 3, 15),
                "Updated transaction memo"
        );

        var updateTransactionEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value() + "/transactions/" + transactionId,
                HttpMethod.PUT,
                new HttpEntity<>(updateTransactionDto, headers),
                TransactionDto.class
        );

        assertThat(updateTransactionEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(updateTransactionEntity.getBody()).isNotNull();
        assertThat(updateTransactionEntity.getBody().id()).isEqualTo(transactionId);

        var updatedAccount = accountRepository.findWithTransactionsByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();

        var updatedTransaction = updatedAccount.get().getTransactions().stream()
                .filter(t -> t.getId().value().equals(transactionId))
                .findFirst()
                .orElseThrow();

        assertThat(updatedTransaction.getAmount().value()).isEqualTo(new BigDecimal("250.00"));
        assertThat(updatedTransaction.getDateOfTransaction().value()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(updatedTransaction.getMemo().value()).isEqualTo("Updated transaction memo");
    }

    @Test
    void removeTransaction_then_remove_transaction_from_account() {
        var transactionToRemove = account.getTransactions().get(1);
        var transactionId = transactionToRemove.getId().value();
        var initialTransactionCount = account.getTransactions().size();

        var removeTransactionEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value() + "/transactions/" + transactionId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(removeTransactionEntity.getStatusCode().value()).isEqualTo(204);

        var updatedAccount = accountRepository.findWithTransactionsByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getTransactions()).hasSize(initialTransactionCount - 1);

        var removedTransactionExists = updatedAccount.get().getTransactions().stream().anyMatch(t -> t.getId().value().equals(transactionId));
        assertThat(removedTransactionExists).isFalse();
    }

    @Test
    void clearTransaction_then_clear_transaction_in_account() {
        var transactionToClear = account.getTransactions().getLast();
        var transactionId = transactionToClear.getId().value();
        var initialClearedStatus = transactionToClear.isCleared();
        
        assertThat(initialClearedStatus).isFalse();

        var clearTransactionEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value() + "/transactions/" + transactionId + "/clear",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(clearTransactionEntity.getStatusCode().value()).isEqualTo(204);

        var updatedAccount = accountRepository.findWithTransactionsByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getTransactions()).hasSize(3);
        
        var clearedTransaction = updatedAccount.get().getTransactions().stream()
                .filter(t -> t.getId().value().equals(transactionId))
                .findFirst()
                .orElseThrow();
                
        assertThat(clearedTransaction.isCleared()).isTrue();
    }

    @Test
    void unclearTransaction_then_unclear_transaction_in_account() {
        var transactionToUnclear = account.getTransactions().getFirst();
        var transactionId = transactionToUnclear.getId().value();
        var initialClearedStatus = transactionToUnclear.isCleared();
        
        assertThat(initialClearedStatus).isTrue();

        var unclearTransactionEntity = testRestTemplate.exchange(
                "/api/accounts/" + account.getId().value() + "/transactions/" + transactionId + "/unclear",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(unclearTransactionEntity.getStatusCode().value()).isEqualTo(204);

        var updatedAccount = accountRepository.findWithTransactionsByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getTransactions()).hasSize(3);
        
        var unclearedTransaction = updatedAccount.get().getTransactions().stream()
                .filter(t -> t.getId().value().equals(transactionId))
                .findFirst()
                .orElseThrow();
                
        assertThat(unclearedTransaction.isCleared()).isFalse();
    }

}