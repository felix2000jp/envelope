package dev.felix2000jp.envelope.appusers.infrastructure.security;

import dev.felix2000jp.envelope.appusers.domain.AppuserRepository;
import dev.felix2000jp.envelope.appusers.domain.exceptions.AppuserNotFoundException;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import dev.felix2000jp.envelope.system.security.SecurityScope;
import dev.felix2000jp.envelope.system.security.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
class AppuserUserDetailsService implements UserDetailsService {

    private final AppuserRepository appuserRepository;

    AppuserUserDetailsService(AppuserRepository appuserRepository) {
        this.appuserRepository = appuserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var usernameValueObject = new Username(username);

        var appuser = appuserRepository
                .findByUsername(usernameValueObject)
                .orElseThrow(AppuserNotFoundException::new);

        return new SecurityUser(
                appuser.getId().value(),
                appuser.getUsername().value(),
                appuser.getPassword().value(),
                appuser.getScopes().stream().map(s -> SecurityScope.valueOf(s.value())).collect(Collectors.toSet())
        );
    }

}
