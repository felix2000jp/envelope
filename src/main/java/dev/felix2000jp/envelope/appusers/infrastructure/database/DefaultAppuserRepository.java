package dev.felix2000jp.envelope.appusers.infrastructure.database;

import dev.felix2000jp.envelope.appusers.domain.Appuser;
import dev.felix2000jp.envelope.appusers.domain.AppuserRepository;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class DefaultAppuserRepository implements AppuserRepository {

    private final AppuserJpaRepository appuserJpaRepository;

    DefaultAppuserRepository(AppuserJpaRepository appuserJpaRepository) {
        this.appuserJpaRepository = appuserJpaRepository;
    }

    @Override
    public Optional<Appuser> findById(AppuserId id) {
        return appuserJpaRepository.findById(id);
    }

    @Override
    public Optional<Appuser> findByUsername(Username username) {
        return appuserJpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return appuserJpaRepository.existsByUsername(username);
    }

    @Override
    public void delete(Appuser appuser) {
        appuserJpaRepository.delete(appuser);
    }

    @Override
    public void deleteAll() {
        appuserJpaRepository.deleteAll();
    }

    @Override
    public void save(Appuser appuser) {
        appuserJpaRepository.save(appuser);
    }

}
