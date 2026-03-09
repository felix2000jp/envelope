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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import({TestcontainersConfiguration.class})
class AccountControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private AccountRepository accountRepository;

    private Account account;
    private String token;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(UUID.randomUUID()),
                new AccountName("Test Account"),
                new AccountBalance(new BigDecimal("1000.00"))
        );

        account.addTransaction(
                new TransactionAmount(new BigDecimal("100.00")),
                new TransactionMemo("Test transaction 1"),
                true
        );
        account.addTransaction(
                new TransactionAmount(new BigDecimal("-100.00")),
                new TransactionMemo("Test transaction 2"),
                false
        );
        account.addTransaction(
                new TransactionAmount(new BigDecimal("250.00")),
                new TransactionDate(LocalDate.of(2024, 3, 1)),
                new TransactionMemo("Groceries"),
                true
        );
        account.addTransaction(
                new TransactionAmount(new BigDecimal("-40.00")),
                new TransactionDate(LocalDate.of(2024, 3, 5)),
                new TransactionMemo("Coffee shop"),
                false
        );

        accountRepository.save(account);
        token = securityService.generateToken(
                account.getUserId().value(),
                "username",
                List.of(SecurityScope.APPLICATION)
        );
    }

    @Test
    void get_then_return_accounts() {
        var getAllAccountsEntity = restTestClient
                .get()
                .uri("/api/accounts")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountListDto.class)
                .returnResult();

        assertThat(getAllAccountsEntity.getResponseBody()).isNotNull();
        assertThat(getAllAccountsEntity.getResponseBody().total()).isEqualTo(1);
    }

    @Test
    void create_then_create_account() {
        var createAccountDto = new CreateAccountDto("New Account", new BigDecimal("500.00"));

        var createAccountEntity = restTestClient
                .post()
                .uri("/api/accounts")
                .headers(h -> h.setBearerAuth(token))
                .body(createAccountDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(AccountDto.class)
                .returnResult();

        assertThat(createAccountEntity.getResponseBody()).isNotNull();
        assertThat(createAccountEntity.getResponseBody().name()).isEqualTo("New Account");
        assertThat(createAccountEntity.getResponseBody().balance()).isEqualTo(new BigDecimal("500.00"));
        assertThat(createAccountEntity.getResponseBody().closed()).isFalse();

        var createdAccountId = new AccountId(createAccountEntity.getResponseBody().id());
        var createdAccount = accountRepository.findByIdAndUserId(createdAccountId, account.getUserId());
        assertThat(createdAccount).isPresent();
        assertThat(createdAccount.get().getName().value()).isEqualTo("New Account");
        assertThat(createdAccount.get().getBalance().value()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void update_then_update_account() {
        var updateAccountDto = new UpdateAccountDto("Updated Account Name", new BigDecimal("2000.00"));

        var updateAccountEntity = restTestClient
                .put()
                .uri("/api/accounts/" + account.getId().value())
                .headers(h -> h.setBearerAuth(token))
                .body(updateAccountDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountDto.class)
                .returnResult();

        assertThat(updateAccountEntity.getResponseBody()).isNotNull();
        assertThat(updateAccountEntity.getResponseBody().id()).isEqualTo(account.getId().value());
        assertThat(updateAccountEntity.getResponseBody().name()).isEqualTo("Updated Account Name");
        assertThat(updateAccountEntity.getResponseBody().balance()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(updateAccountEntity.getResponseBody().closed()).isFalse();

        var updatedAccount = accountRepository.findByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getName().value()).isEqualTo("Updated Account Name");
        assertThat(updatedAccount.get().getBalance().value()).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    void getAccountById_then_return_account() {
        var getAccountByIdEntity = restTestClient
                .get()
                .uri("/api/accounts/" + account.getId().value())
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountDto.class)
                .returnResult();

        assertThat(getAccountByIdEntity.getResponseBody()).isNotNull();
        assertThat(getAccountByIdEntity.getResponseBody().id()).isEqualTo(account.getId().value());
        assertThat(getAccountByIdEntity.getResponseBody().name()).isEqualTo("Test Account");
        assertThat(getAccountByIdEntity.getResponseBody().balance()).isEqualTo(new BigDecimal("1350.00"));
        assertThat(getAccountByIdEntity.getResponseBody().closed()).isFalse();
    }

    @Test
    void getTransactions_then_return_transaction_slice() {
        var getTransactionsEntity = restTestClient
                .get()
                .uri("/api/accounts/" + account.getId().value() + "/transactions")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionSliceDto.class)
                .returnResult();

        assertThat(getTransactionsEntity.getResponseBody()).isNotNull();
        assertThat(getTransactionsEntity.getResponseBody().items()).hasSize(5);
        assertThat(getTransactionsEntity.getResponseBody().hasMore()).isFalse();
        assertThat(getTransactionsEntity.getResponseBody().nextCursor()).isNull();
    }

    @Test
    void getTransactions_given_limit_then_return_next_cursor() {
        var firstSliceEntity = restTestClient
                .get()
                .uri("/api/accounts/" + account.getId().value() + "/transactions?limit=2")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionSliceDto.class)
                .returnResult();

        assertThat(firstSliceEntity.getResponseBody()).isNotNull();
        assertThat(firstSliceEntity.getResponseBody().items()).hasSize(2);
        assertThat(firstSliceEntity.getResponseBody().hasMore()).isTrue();
        assertThat(firstSliceEntity.getResponseBody().nextCursor()).isNotBlank();

        var cursor = firstSliceEntity.getResponseBody().nextCursor();

        var secondSliceEntity = restTestClient
                .get()
                .uri("/api/accounts/" + account.getId().value() + "/transactions?limit=2&cursor=" + cursor)
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionSliceDto.class)
                .returnResult();

        assertThat(secondSliceEntity.getResponseBody()).isNotNull();
        assertThat(secondSliceEntity.getResponseBody().items()).hasSize(2);

        var firstIds = firstSliceEntity.getResponseBody().items().stream().map(TransactionDto::id).toList();
        var secondIds = secondSliceEntity.getResponseBody().items().stream().map(TransactionDto::id).toList();

        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }

    @Test
    void getTransactions_given_filters_then_return_filtered_transactions() {
        var filteredSliceEntity = restTestClient
                .get()
                .uri("/api/accounts/" + account.getId().value() + "/transactions?memo=coffee&cleared=false&minAmount=-50&maxAmount=0&sort=asc")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionSliceDto.class)
                .returnResult();

        assertThat(filteredSliceEntity.getResponseBody()).isNotNull();
        assertThat(filteredSliceEntity.getResponseBody().items()).hasSize(1);

        var transaction = filteredSliceEntity.getResponseBody().items().getFirst();
        assertThat(transaction.memo()).isEqualTo("Coffee shop");
        assertThat(transaction.cleared()).isFalse();
        assertThat(transaction.amount()).isEqualTo(new BigDecimal("-40.00"));
    }

    @Test
    void addTransaction_then_add_transaction_to_account() {
        var addTransactionDto = new AddTransactionDto(
                new BigDecimal("200.00"),
                LocalDate.now(),
                "Test transaction via API",
                true
        );

        var addTransactionEntity = restTestClient
                .post()
                .uri("/api/accounts/" + account.getId().value() + "/transactions")
                .headers(h -> h.setBearerAuth(token))
                .body(addTransactionDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(TransactionDto.class)
                .returnResult();

        assertThat(addTransactionEntity.getResponseBody()).isNotNull();
        assertThat(addTransactionEntity.getResponseBody().amount()).isEqualTo(new BigDecimal("200.00"));

        var updatedAccount = accountRepository.findByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getBalance().value()).isEqualTo(new BigDecimal("1550.00"));
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

        var updateTransactionEntity = restTestClient
                .put()
                .uri("/api/accounts/" + account.getId().value() + "/transactions/" + transactionId)
                .headers(h -> h.setBearerAuth(token))
                .body(updateTransactionDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionDto.class)
                .returnResult();

        assertThat(updateTransactionEntity.getResponseBody()).isNotNull();
        assertThat(updateTransactionEntity.getResponseBody().id()).isEqualTo(transactionId);

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

        restTestClient
                .delete()
                .uri("/api/accounts/" + account.getId().value() + "/transactions/" + transactionId)
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

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

        restTestClient
                .patch()
                .uri("/api/accounts/" + account.getId().value() + "/transactions/" + transactionId + "/clear")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        var updatedAccount = accountRepository.findWithTransactionsByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getTransactions()).hasSize(5);

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

        restTestClient
                .patch()
                .uri("/api/accounts/" + account.getId().value() + "/transactions/" + transactionId + "/unclear")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        var updatedAccount = accountRepository.findWithTransactionsByIdAndUserId(account.getId(), account.getUserId());
        assertThat(updatedAccount).isPresent();
        assertThat(updatedAccount.get().getTransactions()).hasSize(5);

        var unclearedTransaction = updatedAccount.get().getTransactions().stream()
                .filter(t -> t.getId().value().equals(transactionId))
                .findFirst()
                .orElseThrow();

        assertThat(unclearedTransaction.isCleared()).isFalse();
    }

}
