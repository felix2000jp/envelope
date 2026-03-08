package dev.felix2000jp.envelope.accounts.infrastructure.database;

import dev.felix2000jp.envelope.accounts.application.dtos.GetAccountTransactionsDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionDto;
import dev.felix2000jp.envelope.accounts.application.queries.TransactionQueryCursor;
import dev.felix2000jp.envelope.accounts.application.queries.TransactionQueryRepository;
import dev.felix2000jp.envelope.accounts.application.queries.TransactionQuerySortDirection;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
class DefaultTransactionQueryRepository implements TransactionQueryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    DefaultTransactionQueryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<TransactionDto> findByAccountIdAndUserId(
            UUID accountId,
            UUID userId,
            GetAccountTransactionsDto query,
            TransactionQuerySortDirection sort,
            TransactionQueryCursor cursor,
            int limit
    ) {
        var memoFilter = normalizeMemo(query.memo());
        var params = new MapSqlParameterSource()
                .addValue("accountId", accountId)
                .addValue("userId", userId)
                .addValue("limit", limit);

        var whereClauses = new ArrayList<String>();
        whereClauses.add("a.id = :accountId");
        whereClauses.add("a.user_id = :userId");

        if (memoFilter != null) {
            whereClauses.add("t.memo ILIKE :memo ESCAPE E'\\\\'");
            params.addValue("memo", "%" + escapeLikePattern(memoFilter) + "%");
        }

        if (query.cleared() != null) {
            whereClauses.add("t.cleared = :cleared");
            params.addValue("cleared", query.cleared());
        }

        if (query.minAmount() != null) {
            whereClauses.add("t.amount >= :minAmount");
            params.addValue("minAmount", query.minAmount());
        }

        if (query.maxAmount() != null) {
            whereClauses.add("t.amount <= :maxAmount");
            params.addValue("maxAmount", query.maxAmount());
        }

        if (cursor != null) {
            if (sort == TransactionQuerySortDirection.DESC) {
                whereClauses.add("(t.date_of_transaction, t.id) < (:cursorDate, :cursorId)");
            } else {
                whereClauses.add("(t.date_of_transaction, t.id) > (:cursorDate, :cursorId)");
            }

            params.addValue("cursorDate", cursor.dateOfTransaction());
            params.addValue("cursorId", cursor.transactionId());
        }

        var direction = sort == TransactionQuerySortDirection.DESC ? "DESC" : "ASC";
        var sql = """
                SELECT t.id, t.amount, t.date_of_transaction, t.memo, t.cleared
                FROM transaction t
                JOIN account a ON a.id = t.account_id
                WHERE %s
                ORDER BY t.date_of_transaction %s, t.id %s
                LIMIT :limit
                """.formatted(String.join(" AND ", whereClauses), direction, direction);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> new TransactionDto(
                rs.getObject("id", UUID.class),
                rs.getBigDecimal("amount"),
                rs.getObject("date_of_transaction", java.time.LocalDate.class),
                rs.getString("memo"),
                rs.getBoolean("cleared")
        ));
    }

    private String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }

        return memo.trim();
    }

    private String escapeLikePattern(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
