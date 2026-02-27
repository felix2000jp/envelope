package dev.felix2000jp.envelope.accounts.application.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(
        UUID id,
        String name,
        BigDecimal balance,
        boolean closed
) {
}
