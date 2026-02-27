package dev.felix2000jp.envelope.accounts.application.dtos;

import java.util.List;

public record TransactionListDto(int total, List<TransactionDto> transactions) {
}
