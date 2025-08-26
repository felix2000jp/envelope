package dev.felix2000jp.envelope.accounts.infrastructure.api;

import dev.felix2000jp.envelope.accounts.application.AccountService;
import dev.felix2000jp.envelope.accounts.application.dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    ResponseEntity<AccountListDto> get() {
        var body = accountService.get();
        return ResponseEntity.ok(body);
    }

    @PostMapping
    ResponseEntity<AccountDto> create(@Valid @RequestBody CreateAccountDto createAccountDto) {
        var body = accountService.create(createAccountDto.name(), createAccountDto.initialBalance());
        var location = URI.create("/api/accounts/" + body.id());
        return ResponseEntity.created(location).body(body);
    }

    @PutMapping("/{id}")
    ResponseEntity<AccountDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateAccountDto updateAccountDto) {
        var body = accountService.update(id, updateAccountDto);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> getAccountById(@PathVariable UUID id) {
        var body = accountService.getAccountById(id);
        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{id}/close")
    ResponseEntity<Void> closeAccount(@PathVariable UUID id) {
        accountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/open")
    ResponseEntity<Void> openAccount(@PathVariable UUID id) {
        accountService.openAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    ResponseEntity<TransactionListDto> getAccountTransactions(@PathVariable UUID id) {
        var body = accountService.getAccountTransactions(id);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/transactions")
    ResponseEntity<TransactionDto> addTransaction(@PathVariable UUID id, @Valid @RequestBody AddTransactionDto addTransactionDto) {
        var transaction = accountService.addTransaction(id, addTransactionDto);
        var location = URI.create("/api/accounts/" + id + "/transactions");
        return ResponseEntity.created(location).body(transaction);
    }

    @PutMapping("/{accountId}/transactions/{transactionId}")
    ResponseEntity<TransactionDto> updateTransaction(@PathVariable UUID accountId, @PathVariable UUID transactionId, @Valid @RequestBody UpdateTransactionDto updateTransactionDto) {
        var transaction = accountService.updateTransaction(accountId, transactionId, updateTransactionDto);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{accountId}/transactions/{transactionId}")
    ResponseEntity<Void> removeTransaction(@PathVariable UUID accountId, @PathVariable UUID transactionId) {
        accountService.removeTransaction(accountId, transactionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountId}/transactions/{transactionId}/clear")
    ResponseEntity<Void> clearTransaction(@PathVariable UUID accountId, @PathVariable UUID transactionId) {
        accountService.clearTransaction(accountId, transactionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountId}/transactions/{transactionId}/unclear")
    ResponseEntity<Void> unclearTransaction(@PathVariable UUID accountId, @PathVariable UUID transactionId) {
        accountService.unclearTransaction(accountId, transactionId);
        return ResponseEntity.noContent().build();
    }

}
