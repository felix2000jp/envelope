package dev.felix2000jp.envelope.accounts.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDto(
        UUID id,
        BigDecimal amount,
        LocalDate dateOfTransaction,
        String memo,
        boolean cleared
) {
}