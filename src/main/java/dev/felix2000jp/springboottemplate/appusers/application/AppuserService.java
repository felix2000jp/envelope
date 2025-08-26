package dev.felix2000jp.springboottemplate.appusers.application;

import dev.felix2000jp.springboottemplate.appusers.application.dtos.AppuserDto;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.CreateAppuserDto;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.UpdateAppuserDto;
import dev.felix2000jp.springboottemplate.appusers.domain.Appuser;
import dev.felix2000jp.springboottemplate.appusers.domain.AppuserRepository;
import dev.felix2000jp.springboottemplate.appusers.domain.exceptions.AppuserAlreadyExistsException;
import dev.felix2000jp.springboottemplate.appusers.domain.exceptions.AppuserNotFoundException;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.Password;
import dev.felix2000jp.springboottemplate.appusers.domain.valueobjects.Username;
import dev.felix2000jp.springboottemplate.system.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AppuserService {

    private static final Logger log = LoggerFactory.getLogger(AppuserService.class);

    private final SecurityService securityService;
    private final AppuserRepository appuserRepository;
    private final AppuserMapper appuserMapper;

    AppuserService(SecurityService securityService, AppuserRepository appuserRepository, AppuserMapper appuserMapper) {
        this.securityService = securityService;
        this.appuserRepository = appuserRepository;
        this.appuserMapper = appuserMapper;
    }

    @Transactional(readOnly = true)
    public AppuserDto get() {
        var user = securityService.loadUserFromSecurityContext();
        var idValueObject = new AppuserId(user.id());

        var appuser = appuserRepository
                .findById(idValueObject)
                .orElseThrow(AppuserNotFoundException::new);

        return appuserMapper.toDto(appuser);
    }

    @Transactional
    public void create(CreateAppuserDto createAppuserDto) {
        var appuserToCreate = Appuser.from(
                new AppuserId(UUID.randomUUID()),
                new Username(createAppuserDto.username()),
                new Password(securityService.generateEncodedPassword(createAppuserDto.password()))
        );
        appuserToCreate.addApplicationScope();

        var doesUsernameExist = appuserRepository.existsByUsername(appuserToCreate.getUsername());
        if (doesUsernameExist) {
            throw new AppuserAlreadyExistsException();
        }

        appuserRepository.save(appuserToCreate);
        log.info("Appuser with id {} created", appuserToCreate.getId());
    }

    @Transactional
    public void update(UpdateAppuserDto updateAppuserDto) {
        var user = securityService.loadUserFromSecurityContext();
        var idValueObject = new AppuserId(user.id());
        var usernameValueObject = new Username(updateAppuserDto.username());
        var passwordValueObject = new Password(securityService.generateEncodedPassword(updateAppuserDto.password()));

        var appuserToUpdate = appuserRepository
                .findById(idValueObject)
                .orElseThrow(AppuserNotFoundException::new);

        var isUsernameNew = !usernameValueObject.equals(appuserToUpdate.getUsername());
        var doesUsernameExist = appuserRepository.existsByUsername(usernameValueObject);

        if (isUsernameNew && doesUsernameExist) {
            throw new AppuserAlreadyExistsException();
        }

        appuserToUpdate.setUsername(usernameValueObject);
        appuserToUpdate.setPassword(passwordValueObject);
        appuserRepository.save(appuserToUpdate);
        log.info("Appuser with id {} updated", appuserToUpdate.getId());
    }

    @Transactional
    public void delete() {
        var user = securityService.loadUserFromSecurityContext();
        var idValueObject = new AppuserId(user.id());

        var appuserToDelete = appuserRepository
                .findById(idValueObject)
                .orElseThrow(AppuserNotFoundException::new);

        appuserToDelete.delete();
        appuserRepository.delete(appuserToDelete);
        log.info("Appuser with id {} deleted", appuserToDelete.getId());
    }

    @Transactional(readOnly = true)
    public String login() {
        var user = securityService.loadUserFromSecurityContext();
        return securityService.generateToken(user.id(), user.username(), user.scopes());
    }

}
