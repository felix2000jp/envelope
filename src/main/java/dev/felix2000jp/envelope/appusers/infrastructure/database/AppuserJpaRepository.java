package dev.felix2000jp.envelope.appusers.infrastructure.database;

import dev.felix2000jp.envelope.appusers.domain.Appuser;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface AppuserJpaRepository extends JpaRepository<Appuser, AppuserId> {

    Optional<Appuser> findByUsername(Username username);

    boolean existsByUsername(Username username);

}