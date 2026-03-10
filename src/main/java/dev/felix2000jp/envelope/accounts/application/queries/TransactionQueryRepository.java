package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.dtos.GetTransactionsDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionDto;

import java.util.List;
import java.util.UUID;

public interface TransactionQueryRepository {

    List<TransactionDto> findByAccountIdAndUserId(
            UUID accountId,
            UUID userId,
            GetTransactionsDto query,
            TransactionQuerySortDirection sort,
            TransactionQueryCursor cursor,
            int limit
    );
}
