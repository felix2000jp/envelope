package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.exceptions.InvalidTransactionQueryException;

public enum TransactionQuerySortDirection {
    ASC,
    DESC;

    public static TransactionQuerySortDirection from(String value) {
        if (value == null || value.isBlank()) {
            return DESC;
        }

        return switch (value.trim().toUpperCase()) {
            case "ASC" -> ASC;
            case "DESC" -> DESC;
            default -> throw new InvalidTransactionQueryException("Invalid sort direction. Use 'asc' or 'desc'");
        };
    }
}
