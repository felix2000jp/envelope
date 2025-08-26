package dev.felix2000jp.envelope.accounts.infrastructure.database;

import dev.felix2000jp.envelope.accounts.domain.Account;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.AccountId;
import dev.felix2000jp.envelope.accounts.domain.valueobjects.UserId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountJpaRepository extends JpaRepository<Account, AccountId> {

    List<Account> findAllByUserId(UserId userId);

    Optional<Account> findByIdAndUserId(AccountId id, UserId userId);

    @EntityGraph(attributePaths = "transactions")
    Optional<Account> findWithTransactionsByIdAndUserId(AccountId id, UserId userId);

    void deleteAllByUserId(UserId userId);
}
