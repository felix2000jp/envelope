package dev.felix2000jp.envelope.accounts.infrastructure.database;

import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.AccountRepository;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DefaultAccountRepository implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    public DefaultAccountRepository(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public List<Account> findAllByUserId(UserId userId) {
        return accountJpaRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Account> findByIdAndUserId(AccountId id, UserId userId) {
        return accountJpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public Optional<Account> findWithTransactionsByIdAndUserId(AccountId id, UserId userId) {
        return accountJpaRepository.findWithTransactionsByIdAndUserId(id, userId);
    }

    @Override
    public void deleteAll() {
        accountJpaRepository.deleteAll();
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        accountJpaRepository.deleteAllByUserId(userId);
    }

    @Override
    public void delete(Account account) {
        accountJpaRepository.delete(account);
    }

    @Override
    public void save(Account account) {
        accountJpaRepository.save(account);
    }
}
