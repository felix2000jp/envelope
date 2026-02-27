package dev.felix2000jp.envelope.accounts.application.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountDto(
        @NotBlank
        @Size(min = 1, max = 255)
        String name,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal initialBalance
) {
}