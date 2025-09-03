package dev.felix2000jp.envelope.accounts.domain;

import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import org.jmolecules.ddd.types.Repository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends Repository<Account, AccountId> {

    List<Account> findAllByUserId(UserId userId);

    Optional<Account> findByIdAndUserId(AccountId id, UserId userId);

    Optional<Account> findWithTransactionsByIdAndUserId(AccountId id, UserId userId);

    void deleteAll();

    void deleteAllByUserId(UserId userId);

    void delete(Account account);

    void save(Account account);

}
