package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.exceptions.InvalidTransactionQueryException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionQueryCursorTest {

    @Test
    void encodeDecode_roundTrip_returnsSameCursor() {
        var cursor = new TransactionQueryCursor(
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 8),
                TransactionQuerySortDirection.DESC
        );

        var encoded = TransactionQueryCursor.encode(cursor);
        var decoded = TransactionQueryCursor.decode(encoded, TransactionQuerySortDirection.DESC);

        assertThat(decoded).isEqualTo(cursor);
    }

    @Test
    void decode_givenNullOrBlank_returnsNull() {
        assertThat(TransactionQueryCursor.decode(null, TransactionQuerySortDirection.DESC)).isNull();
        assertThat(TransactionQueryCursor.decode("  ", TransactionQuerySortDirection.DESC)).isNull();
    }

    @Test
    void decode_givenMalformedCursor_throwsInvalidTransactionQueryException() {
        var invalidBase64 = "not-a-valid-cursor";

        assertThatThrownBy(() -> TransactionQueryCursor.decode(invalidBase64, TransactionQuerySortDirection.DESC))
                .isInstanceOf(InvalidTransactionQueryException.class)
                .hasMessage("Invalid cursor format");
    }

    @Test
    void decode_givenSortMismatch_throwsInvalidTransactionQueryException() {
        var cursor = new TransactionQueryCursor(
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 8),
                TransactionQuerySortDirection.DESC
        );
        var encoded = TransactionQueryCursor.encode(cursor);

        assertThatThrownBy(() -> TransactionQueryCursor.decode(encoded, TransactionQuerySortDirection.ASC))
                .isInstanceOf(InvalidTransactionQueryException.class)
                .hasMessage("Cursor sort does not match request sort");
    }
}
