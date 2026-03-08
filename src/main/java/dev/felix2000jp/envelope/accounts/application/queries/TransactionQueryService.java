package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.dtos.GetAccountTransactionsDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionSliceDto;
import dev.felix2000jp.envelope.accounts.application.exceptions.InvalidTransactionQueryException;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.exceptions.AccountNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import dev.felix2000jp.envelope.system.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionQueryService {

    private static final int MAX_LIMIT = 100;

    private final SecurityService securityService;
    private final AccountRepository accountRepository;
    private final TransactionQueryRepository transactionQueryRepository;

    TransactionQueryService(
            SecurityService securityService,
            AccountRepository accountRepository,
            TransactionQueryRepository transactionQueryRepository
    ) {
        this.securityService = securityService;
        this.accountRepository = accountRepository;
        this.transactionQueryRepository = transactionQueryRepository;
    }

    @Transactional(readOnly = true)
    public TransactionSliceDto getAccountTransactions(UUID accountId, GetAccountTransactionsDto request) {
        validateAmountRange(request);

        var user = securityService.loadUserFromSecurityContext();
        var userId = new UserId(user.id());
        var account = accountRepository.findByIdAndUserId(new AccountId(accountId), userId);

        if (account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        var sort = TransactionQuerySortDirection.from(request.sort());
        var cursor = TransactionQueryCursor.decode(request.cursor(), sort);
        var limit = normalizeLimit(request.limit());

        var fetchedItems = transactionQueryRepository.findByAccountIdAndUserId(
                accountId,
                user.id(),
                request,
                sort,
                cursor,
                limit + 1
        );

        var hasMore = fetchedItems.size() > limit;
        var items = hasMore ? fetchedItems.subList(0, limit) : fetchedItems;

        String nextCursor = null;
        if (hasMore && !items.isEmpty()) {
            var lastItem = items.getLast();
            nextCursor = TransactionQueryCursor.encode(new TransactionQueryCursor(lastItem.id(), lastItem.dateOfTransaction(), sort));
        }

        return new TransactionSliceDto(items, nextCursor, hasMore);
    }

    private int normalizeLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidTransactionQueryException("Invalid limit. Allowed range is 1 to 100");
        }

        return limit;
    }

    private void validateAmountRange(GetAccountTransactionsDto request) {
        if (request.minAmount() != null && request.maxAmount() != null && request.minAmount().compareTo(request.maxAmount()) > 0) {
            throw new InvalidTransactionQueryException("minAmount must be less than or equal to maxAmount");
        }
    }
}
