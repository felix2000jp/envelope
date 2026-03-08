package dev.felix2000jp.envelope.accounts.infrastructure.database;

import dev.felix2000jp.envelope.TestcontainersConfiguration;
import dev.felix2000jp.envelope.accounts.application.dtos.GetAccountTransactionsDto;
import dev.felix2000jp.envelope.accounts.application.queries.TransactionQueryCursor;
import dev.felix2000jp.envelope.accounts.application.queries.TransactionQueryRepository;
import dev.felix2000jp.envelope.accounts.application.queries.TransactionQuerySortDirection;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.Transaction;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountBalance;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountName;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionAmount;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionDate;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionMemo;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, DefaultAccountRepository.class, DefaultTransactionQueryRepository.class})
class DefaultTransactionQueryRepositoryIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID OTHER_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");

    @Autowired
    private DefaultAccountRepository accountRepository;
    @Autowired
    private TransactionQueryRepository transactionQueryRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        var account = Account.from(
                new AccountId(ACCOUNT_ID),
                new UserId(USER_ID),
                new AccountName("Main"),
                new AccountBalance(BigDecimal.ZERO)
        );

        account.getTransactions().addAll(List.of(
                Transaction.from(
                        new TransactionId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
                        new TransactionAmount(new BigDecimal("10.00")),
                        new TransactionDate(LocalDate.of(2024, 3, 10)),
                        new TransactionMemo("Coffee shop"),
                        false
                ),
                Transaction.from(
                        new TransactionId(UUID.fromString("00000000-0000-0000-0000-000000000102")),
                        new TransactionAmount(new BigDecimal("20.00")),
                        new TransactionDate(LocalDate.of(2024, 3, 10)),
                        new TransactionMemo("GROCERIES"),
                        true
                ),
                Transaction.from(
                        new TransactionId(UUID.fromString("00000000-0000-0000-0000-000000000103")),
                        new TransactionAmount(new BigDecimal("30.00")),
                        new TransactionDate(LocalDate.of(2024, 3, 11)),
                        new TransactionMemo("Rent"),
                        true
                ),
                Transaction.from(
                        new TransactionId(UUID.fromString("00000000-0000-0000-0000-000000000104")),
                        new TransactionAmount(new BigDecimal("40.00")),
                        new TransactionDate(LocalDate.of(2024, 3, 12)),
                        new TransactionMemo("Salary"),
                        true
                ),
                Transaction.from(
                        new TransactionId(UUID.fromString("00000000-0000-0000-0000-000000000105")),
                        new TransactionAmount(new BigDecimal("-15.00")),
                        new TransactionDate(LocalDate.of(2024, 3, 13)),
                        new TransactionMemo("Coffee beans"),
                        false
                )
        ));

        var otherAccount = Account.from(
                new AccountId(OTHER_ACCOUNT_ID),
                new UserId(OTHER_USER_ID),
                new AccountName("Other"),
                new AccountBalance(BigDecimal.ZERO)
        );
        otherAccount.getTransactions().add(
                Transaction.from(
                        new TransactionId(UUID.fromString("00000000-0000-0000-0000-000000000201")),
                        new TransactionAmount(new BigDecimal("999.00")),
                        new TransactionDate(LocalDate.of(2024, 3, 10)),
                        new TransactionMemo("Should not leak"),
                        true
                )
        );

        accountRepository.save(account);
        accountRepository.save(otherAccount);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByAccountIdAndUserId_returnsOnlyRequestedAccountAndUserTransactions() {
        var query = new GetAccountTransactionsDto(50, "desc", null, null, null, null, null);

        var actual = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                null,
                50
        );

        assertThat(actual).hasSize(5);
        assertThat(actual).noneMatch(t -> t.memo().equals("Should not leak"));
    }

    @Test
    void findByAccountIdAndUserId_sortsByDateThenId_forAscAndDesc() {
        var query = new GetAccountTransactionsDto(50, "desc", null, null, null, null, null);

        var desc = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                null,
                50
        );

        var asc = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.ASC,
                null,
                50
        );

        assertThat(desc).extracting(t -> t.id().toString())
                .containsExactly(
                        "00000000-0000-0000-0000-000000000105",
                        "00000000-0000-0000-0000-000000000104",
                        "00000000-0000-0000-0000-000000000103",
                        "00000000-0000-0000-0000-000000000102",
                        "00000000-0000-0000-0000-000000000101"
                );

        assertThat(asc).extracting(t -> t.id().toString())
                .containsExactly(
                        "00000000-0000-0000-0000-000000000101",
                        "00000000-0000-0000-0000-000000000102",
                        "00000000-0000-0000-0000-000000000103",
                        "00000000-0000-0000-0000-000000000104",
                        "00000000-0000-0000-0000-000000000105"
                );
    }

    @Test
    void findByAccountIdAndUserId_appliesCursor_forDescAndAsc() {
        var descCursor = new TransactionQueryCursor(
                UUID.fromString("00000000-0000-0000-0000-000000000104"),
                LocalDate.of(2024, 3, 12),
                TransactionQuerySortDirection.DESC
        );
        var ascCursor = new TransactionQueryCursor(
                UUID.fromString("00000000-0000-0000-0000-000000000102"),
                LocalDate.of(2024, 3, 10),
                TransactionQuerySortDirection.ASC
        );
        var query = new GetAccountTransactionsDto(50, "desc", null, null, null, null, null);

        var desc = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                descCursor,
                50
        );
        var asc = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.ASC,
                ascCursor,
                50
        );

        assertThat(desc).extracting(t -> t.id().toString())
                .containsExactly(
                        "00000000-0000-0000-0000-000000000103",
                        "00000000-0000-0000-0000-000000000102",
                        "00000000-0000-0000-0000-000000000101"
                );
        assertThat(asc).extracting(t -> t.id().toString())
                .containsExactly(
                        "00000000-0000-0000-0000-000000000103",
                        "00000000-0000-0000-0000-000000000104",
                        "00000000-0000-0000-0000-000000000105"
                );
    }

    @Test
    void findByAccountIdAndUserId_appliesMemoFilter_caseInsensitiveAndTrimmed() {
        var query = new GetAccountTransactionsDto(50, "desc", null, null, null, "  CoFfEe ", null);

        var actual = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                null,
                50
        );

        assertThat(actual).extracting(t -> t.id().toString())
                .containsExactly(
                        "00000000-0000-0000-0000-000000000105",
                        "00000000-0000-0000-0000-000000000101"
                );
    }

    @Test
    void findByAccountIdAndUserId_treatsMemoWildcardsAsLiteralCharacters() {
        var query = new GetAccountTransactionsDto(50, "desc", null, null, null, "%", null);

        var actual = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                null,
                50
        );

        assertThat(actual).isEmpty();
    }

    @Test
    void findByAccountIdAndUserId_appliesClearedAndAmountFiltersAndLimit() {
        var query = new GetAccountTransactionsDto(50, "desc", null, new BigDecimal("15.00"), new BigDecimal("40.00"), null, true);

        var filtered = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                null,
                50
        );

        assertThat(filtered).extracting(t -> t.id().toString())
                .containsExactly(
                        "00000000-0000-0000-0000-000000000104",
                        "00000000-0000-0000-0000-000000000103",
                        "00000000-0000-0000-0000-000000000102"
                );

        var limited = transactionQueryRepository.findByAccountIdAndUserId(
                ACCOUNT_ID,
                USER_ID,
                query,
                TransactionQuerySortDirection.DESC,
                null,
                2
        );

        assertThat(limited).hasSize(2);
    }
}
