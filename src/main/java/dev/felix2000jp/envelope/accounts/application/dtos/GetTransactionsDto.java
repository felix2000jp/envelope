package dev.felix2000jp.envelope.accounts.application.dtos;

import java.math.BigDecimal;

public record GetTransactionsDto(
        int limit,
        String sort,
        String cursor,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String memo,
        Boolean cleared
) {
}
