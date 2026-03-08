package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.exceptions.InvalidTransactionQueryException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public record TransactionQueryCursor(
        UUID transactionId,
        LocalDate dateOfTransaction,
        TransactionQuerySortDirection sort
) {

    public static String encode(TransactionQueryCursor cursor) {
        var rawCursor = "%s|%s|%s".formatted(cursor.dateOfTransaction(), cursor.transactionId(), cursor.sort());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    public static TransactionQueryCursor decode(String encodedCursor, TransactionQuerySortDirection expectedSort) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            var rawCursor = new String(Base64.getUrlDecoder().decode(encodedCursor), StandardCharsets.UTF_8);
            var parts = rawCursor.split("\\|", -1);

            if (parts.length != 3) {
                throw new InvalidTransactionQueryException("Invalid cursor format");
            }

            var date = LocalDate.parse(parts[0]);
            var transactionId = UUID.fromString(parts[1]);
            var sort = TransactionQuerySortDirection.from(parts[2]);

            if (sort != expectedSort) {
                throw new InvalidTransactionQueryException("Cursor sort does not match request sort");
            }

            return new TransactionQueryCursor(transactionId, date, sort);
        } catch (InvalidTransactionQueryException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new InvalidTransactionQueryException("Invalid cursor format");
        }
    }
}
