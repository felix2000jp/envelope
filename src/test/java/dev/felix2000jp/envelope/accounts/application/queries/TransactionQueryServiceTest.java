package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.dtos.GetAccountTransactionsDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionDto;
import dev.felix2000jp.envelope.accounts.application.exceptions.InvalidTransactionQueryException;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.exceptions.AccountNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountBalance;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountName;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import dev.felix2000jp.envelope.system.security.SecurityService;
import dev.felix2000jp.envelope.system.security.SecurityUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
class TransactionQueryServiceTest {

    @Mock
    private SecurityService securityService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionQueryRepository transactionQueryRepository;
    @InjectMocks
    private TransactionQueryService transactionQueryService;

    @Captor
    private ArgumentCaptor<Integer> limitCaptor;

    @Test
    void getAccountTransactions_givenAccountNotFound_throwsAccountNotFoundException() {
        var accountId = UUID.randomUUID();
        var user = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());

        when(securityService.loadUserFromSecurityContext()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))).thenReturn(Optional.empty());

        var request = new GetAccountTransactionsDto(30, "desc", null, null, null, null, null);

        assertThatThrownBy(() -> transactionQueryService.getAccountTransactions(accountId, request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccountTransactions_givenMinGreaterThanMax_throwsInvalidTransactionQueryException() {
        var accountId = UUID.randomUUID();
        var request = new GetAccountTransactionsDto(30, "desc", null, new BigDecimal("50"), new BigDecimal("10"), null, null);

        assertThatThrownBy(() -> transactionQueryService.getAccountTransactions(accountId, request))
                .isInstanceOf(InvalidTransactionQueryException.class)
                .hasMessage("minAmount must be less than or equal to maxAmount");
    }

    @Test
    void getAccountTransactions_givenLimitOutOfRange_throwsInvalidTransactionQueryException() {
        var accountId = UUID.randomUUID();
        var user = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var account = Account.from(
                new AccountId(accountId),
                new UserId(user.id()),
                new AccountName("Test"),
                new AccountBalance(BigDecimal.ZERO)
        );

        when(securityService.loadUserFromSecurityContext()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))).thenReturn(Optional.of(account));

        var tooSmall = new GetAccountTransactionsDto(0, "desc", null, null, null, null, null);
        var tooBig = new GetAccountTransactionsDto(101, "desc", null, null, null, null, null);

        assertThatThrownBy(() -> transactionQueryService.getAccountTransactions(accountId, tooSmall))
                .isInstanceOf(InvalidTransactionQueryException.class)
                .hasMessage("Invalid limit. Allowed range is 1 to 100");

        assertThatThrownBy(() -> transactionQueryService.getAccountTransactions(accountId, tooBig))
                .isInstanceOf(InvalidTransactionQueryException.class)
                .hasMessage("Invalid limit. Allowed range is 1 to 100");
    }

    @Test
    void getAccountTransactions_givenMoreThanLimit_setsHasMoreTrue_andNextCursor() {
        var accountId = UUID.randomUUID();
        var user = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var account = Account.from(
                new AccountId(accountId),
                new UserId(user.id()),
                new AccountName("Test"),
                new AccountBalance(BigDecimal.ZERO)
        );
        var request = new GetAccountTransactionsDto(2, "desc", null, null, null, null, null);

        var transaction1 = new TransactionDto(UUID.randomUUID(), new BigDecimal("10.00"), LocalDate.of(2026, 3, 8), "first", true);
        var transaction2 = new TransactionDto(UUID.randomUUID(), new BigDecimal("20.00"), LocalDate.of(2026, 3, 7), "second", true);
        var transaction3 = new TransactionDto(UUID.randomUUID(), new BigDecimal("30.00"), LocalDate.of(2026, 3, 6), "third", false);

        when(securityService.loadUserFromSecurityContext()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))).thenReturn(Optional.of(account));
        when(transactionQueryRepository.findByAccountIdAndUserId(accountId, user.id(), request, TransactionQuerySortDirection.DESC, null, 3))
                .thenReturn(List.of(transaction1, transaction2, transaction3));

        var actual = transactionQueryService.getAccountTransactions(accountId, request);

        assertThat(actual.items()).hasSize(2);
        assertThat(actual.hasMore()).isTrue();
        assertThat(actual.nextCursor()).isNotBlank();
    }

    @Test
    void getAccountTransactions_givenAtMostLimit_setsHasMoreFalse_andNextCursorNull() {
        var accountId = UUID.randomUUID();
        var user = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var account = Account.from(
                new AccountId(accountId),
                new UserId(user.id()),
                new AccountName("Test"),
                new AccountBalance(BigDecimal.ZERO)
        );
        var request = new GetAccountTransactionsDto(2, "asc", null, null, null, null, null);

        var transaction1 = new TransactionDto(UUID.randomUUID(), new BigDecimal("10.00"), LocalDate.of(2026, 3, 8), "first", true);
        var transaction2 = new TransactionDto(UUID.randomUUID(), new BigDecimal("20.00"), LocalDate.of(2026, 3, 9), "second", true);

        when(securityService.loadUserFromSecurityContext()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))).thenReturn(Optional.of(account));
        when(transactionQueryRepository.findByAccountIdAndUserId(accountId, user.id(), request, TransactionQuerySortDirection.ASC, null, 3))
                .thenReturn(List.of(transaction1, transaction2));

        var actual = transactionQueryService.getAccountTransactions(accountId, request);

        assertThat(actual.items()).hasSize(2);
        assertThat(actual.hasMore()).isFalse();
        assertThat(actual.nextCursor()).isNull();
    }

    @Test
    void getAccountTransactions_passesLimitPlusOneToRepository() {
        var accountId = UUID.randomUUID();
        var user = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of());
        var account = Account.from(
                new AccountId(accountId),
                new UserId(user.id()),
                new AccountName("Test"),
                new AccountBalance(BigDecimal.ZERO)
        );
        var request = new GetAccountTransactionsDto(25, "desc", null, null, null, null, null);

        when(securityService.loadUserFromSecurityContext()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))).thenReturn(Optional.of(account));
        when(transactionQueryRepository.findByAccountIdAndUserId(
                accountId,
                user.id(),
                request,
                TransactionQuerySortDirection.DESC,
                null,
                26
        )).thenReturn(List.of());

        transactionQueryService.getAccountTransactions(accountId, request);

        verify(transactionQueryRepository).findByAccountIdAndUserId(
                org.mockito.ArgumentMatchers.eq(accountId),
                org.mockito.ArgumentMatchers.eq(user.id()),
                org.mockito.ArgumentMatchers.eq(request),
                org.mockito.ArgumentMatchers.eq(TransactionQuerySortDirection.DESC),
                org.mockito.ArgumentMatchers.isNull(),
                limitCaptor.capture()
        );
        assertThat(limitCaptor.getValue()).isEqualTo(26);
    }
}
