package dev.felix2000jp.envelope.accounts.application;

import dev.felix2000jp.envelope.accounts.application.dtos.AccountDto;
import dev.felix2000jp.envelope.accounts.application.dtos.AccountListDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionListDto;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountMapper {

    AccountDto toAccountDto(Account account) {
        return new AccountDto(
                account.getId().value(),
                account.getName().value(),
                account.getBalance().value(),
                account.isClosed()
        );
    }

    AccountListDto toAccountListDto(List<Account> accounts) {
        return new AccountListDto(
                accounts.size(),
                accounts.stream().map(this::toAccountDto).toList()
        );
    }

    TransactionDto toTransactionDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId().value(),
                transaction.getAmount().value(),
                transaction.getDateOfTransaction().value(),
                transaction.getMemo().value(),
                transaction.isCleared()
        );
    }

    TransactionListDto toTransactionListDto(List<Transaction> transactions) {
        return new TransactionListDto(
                transactions.size(),
                transactions.stream().map(this::toTransactionDto).toList()
        );
    }

}
