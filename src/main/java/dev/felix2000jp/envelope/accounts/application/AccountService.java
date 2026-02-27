package dev.felix2000jp.envelope.accounts.application;


import dev.felix2000jp.envelope.accounts.application.dtos.AccountDto;
import dev.felix2000jp.envelope.accounts.application.dtos.AccountListDto;
import dev.felix2000jp.envelope.accounts.application.dtos.AddTransactionDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionDto;
import dev.felix2000jp.envelope.accounts.application.dtos.TransactionListDto;
import dev.felix2000jp.envelope.accounts.application.dtos.UpdateAccountDto;
import dev.felix2000jp.envelope.accounts.application.dtos.UpdateTransactionDto;
import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.exceptions.AccountNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountBalance;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountName;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionAmount;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionDate;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.TransactionMemo;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import dev.felix2000jp.envelope.system.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final SecurityService securityService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(
            SecurityService securityService,
            AccountRepository accountRepository,
            AccountMapper accountMapper
    ) {
        this.securityService = securityService;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional(readOnly = true)
    public AccountListDto get() {
        var user = securityService.loadUserFromSecurityContext();
        var accounts = accountRepository.findAllByUserId(new UserId(user.id()));

        return accountMapper.toAccountListDto(accounts);
    }

    @Transactional
    public AccountDto create(String name, BigDecimal initialBalance) {
        var user = securityService.loadUserFromSecurityContext();
        var account = Account.from(
                new AccountId(UUID.randomUUID()),
                new UserId(user.id()),
                new AccountName(name),
                new AccountBalance(initialBalance)
        );

        accountRepository.save(account);
        log.info("Account created with id {} for user {}", account.getId().value(), user.id());

        return accountMapper.toAccountDto(account);
    }

    @Transactional
    public AccountDto update(UUID id, UpdateAccountDto updateAccountDto) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(id), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        account.setName(new AccountName(updateAccountDto.name()));
        account.setBalance(new AccountBalance(updateAccountDto.balance()));

        accountRepository.save(account);
        log.info("Account updated with id {} for user {}", account.getId().value(), user.id());

        return accountMapper.toAccountDto(account);
    }

    @Transactional
    public void delete(UUID id) {
        accountRepository.deleteAllByUserId(new UserId(id));
        log.info("Accounts with userId {} deleted", id);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(UUID id) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(id), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        return accountMapper.toAccountDto(account);
    }

    @Transactional
    public void closeAccount(UUID id) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(id), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        account.close();
        accountRepository.save(account);
        log.info("Account closed with id {} for user {}", account.getId().value(), user.id());
    }

    @Transactional
    public void openAccount(UUID id) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(id), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        account.open();
        accountRepository.save(account);
        log.info("Account opened with id {} for user {}", account.getId().value(), user.id());
    }

    @Transactional(readOnly = true)
    public TransactionListDto getAccountTransactions(UUID id) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(id), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        return accountMapper.toTransactionListDto(account.getTransactions());
    }

    @Transactional
    public TransactionDto addTransaction(UUID id, AddTransactionDto addTransactionDto) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(id), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        var amount = new TransactionAmount(addTransactionDto.amount());
        var date = addTransactionDto.date() != null 
                ? new TransactionDate(addTransactionDto.date()) 
                : new TransactionDate(LocalDate.now());
        var memo = addTransactionDto.memo() != null 
                ? new TransactionMemo(addTransactionDto.memo()) 
                : new TransactionMemo("");

        account.addTransaction(amount, date, memo, addTransactionDto.cleared());
        accountRepository.save(account);
        log.info("Transaction added to account {} for user {}", account.getId().value(), user.id());

        var newTransaction = account.getTransactions().getLast();
        return accountMapper.toTransactionDto(newTransaction);
    }

    @Transactional
    public TransactionDto updateTransaction(UUID accountId, UUID transactionId, UpdateTransactionDto updateTransactionDto) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        var amount = updateTransactionDto.amount() != null 
                ? new TransactionAmount(updateTransactionDto.amount()) 
                : null;
        var date = updateTransactionDto.date() != null 
                ? new TransactionDate(updateTransactionDto.date()) 
                : null;
        var memo = updateTransactionDto.memo() != null 
                ? new TransactionMemo(updateTransactionDto.memo()) 
                : null;

        account.updateTransaction(new TransactionId(transactionId), amount, date, memo);
        accountRepository.save(account);
        log.info("Transaction {} updated in account {} for user {}", transactionId, account.getId().value(), user.id());

        var updatedTransaction = account.getTransactions().stream()
                .filter(t -> t.getId().value().equals(transactionId))
                .findFirst()
                .orElseThrow();
        
        return accountMapper.toTransactionDto(updatedTransaction);
    }

    @Transactional
    public void removeTransaction(UUID accountId, UUID transactionId) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        account.removeTransaction(new TransactionId(transactionId));
        accountRepository.save(account);
        log.info("Transaction {} removed from account {} for user {}", transactionId, account.getId().value(), user.id());
    }

    @Transactional
    public void clearTransaction(UUID accountId, UUID transactionId) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        account.clearTransaction(new TransactionId(transactionId));
        accountRepository.save(account);
        log.info("Transaction {} cleared in account {} for user {}", transactionId, account.getId().value(), user.id());
    }

    @Transactional
    public void unclearTransaction(UUID accountId, UUID transactionId) {
        var user = securityService.loadUserFromSecurityContext();
        var account = accountRepository
                .findByIdAndUserId(new AccountId(accountId), new UserId(user.id()))
                .orElseThrow(AccountNotFoundException::new);

        account.unclearTransaction(new TransactionId(transactionId));
        accountRepository.save(account);
        log.info("Transaction {} uncleared in account {} for user {}", transactionId, account.getId().value(), user.id());
    }

}
