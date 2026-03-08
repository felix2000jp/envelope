package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.dtos.GetAccountTransactionsDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionDto;

import java.util.List;
import java.util.UUID;

public interface TransactionQueryRepository {

    List<TransactionDto> findByAccountIdAndUserId(
            UUID accountId,
            UUID userId,
            GetAccountTransactionsDto query,
            TransactionQuerySortDirection sort,
            TransactionQueryCursor cursor,
            int limit
    );
}
