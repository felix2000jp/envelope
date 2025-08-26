package dev.felix2000jp.envelope.appusers.application;

import dev.felix2000jp.envelope.appusers.application.dtos.AppuserDto;
import dev.felix2000jp.envelope.appusers.domain.Appuser;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Scope;
import org.springframework.stereotype.Component;

@Component
class AppuserMapper {

    AppuserDto toDto(Appuser appuser) {
        return new AppuserDto(
                appuser.getId().value(),
                appuser.getUsername().value(),
                appuser.getScopes().stream().map(Scope::value).toList()
        );
    }

}
