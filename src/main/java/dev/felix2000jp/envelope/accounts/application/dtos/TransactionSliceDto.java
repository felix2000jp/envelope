package dev.felix2000jp.envelope.accounts.application.dtos;

import java.util.List;

public record TransactionSliceDto(List<TransactionDto> items, String nextCursor, boolean hasMore) {
}
