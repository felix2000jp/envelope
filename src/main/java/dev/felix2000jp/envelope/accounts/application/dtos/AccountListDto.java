package dev.felix2000jp.envelope.accounts.application.dtos;

import java.util.List;

public record AccountListDto(int total, List<AccountDto> accounts) {
}
