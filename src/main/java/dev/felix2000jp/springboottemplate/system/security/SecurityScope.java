package dev.felix2000jp.springboottemplate.system.security;

import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.GrantedAuthority;

@NamedInterface
public enum SecurityScope implements GrantedAuthority {

    ADMIN,
    APPLICATION;

    @Override
    public String getAuthority() {
        return "SCOPE_" + name();
    }

}
