package dev.felix2000jp.envelope.accounts.application.dtos;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddTransactionDto(
        @NotNull
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        LocalDate date,

        @Size(max = 255)
        String memo,

        boolean cleared
) {
}
