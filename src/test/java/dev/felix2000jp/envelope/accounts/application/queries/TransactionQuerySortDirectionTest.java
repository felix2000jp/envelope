package dev.felix2000jp.envelope.accounts.application.queries;

import dev.felix2000jp.envelope.accounts.application.exceptions.InvalidTransactionQueryException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionQuerySortDirectionTest {

    @Test
    void from_givenNullOrBlank_returnsDesc() {
        assertThat(TransactionQuerySortDirection.from(null)).isEqualTo(TransactionQuerySortDirection.DESC);
        assertThat(TransactionQuerySortDirection.from("  ")).isEqualTo(TransactionQuerySortDirection.DESC);
    }

    @Test
    void from_givenAscDesc_caseInsensitive_parsesCorrectly() {
        assertThat(TransactionQuerySortDirection.from("asc")).isEqualTo(TransactionQuerySortDirection.ASC);
        assertThat(TransactionQuerySortDirection.from("DESC")).isEqualTo(TransactionQuerySortDirection.DESC);
    }

    @Test
    void from_givenInvalidValue_throwsInvalidTransactionQueryException() {
        assertThatThrownBy(() -> TransactionQuerySortDirection.from("newest"))
                .isInstanceOf(InvalidTransactionQueryException.class)
                .hasMessage("Invalid sort direction. Use 'asc' or 'desc'");
    }
}
