package dev.felix2000jp.springboottemplate.system.security;

import org.springframework.modulith.NamedInterface;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NamedInterface
public class SecurityService {

    private static final String ID_CLAIM_NAME = "id";
    private static final String SCOPE_CLAIM_NAME = "scope";

    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;

    SecurityService(JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateToken(UUID id, String username, Collection<SecurityScope> securityScopes) {
        var now = Instant.now();
        var expiration = now.plus(4, ChronoUnit.HOURS);

        var claims = JwtClaimsSet.builder()
                .issuer("self")
                .subject(username)
                .claim(ID_CLAIM_NAME, id.toString())
                .claim(SCOPE_CLAIM_NAME, String.join(" ", securityScopes.stream().map(Enum::name).toList()))
                .issuedAt(now)
                .expiresAt(expiration)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public SecurityUser loadUserFromSecurityContext() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication.getPrincipal();

        if (principal instanceof SecurityUser securityUser) {
            return securityUser;
        }

        if (principal instanceof Jwt jwt) {
            var idClaim = jwt.getClaimAsString(ID_CLAIM_NAME);
            var id = UUID.fromString(idClaim);

            var scopeClaims = jwt.getClaimAsString(SCOPE_CLAIM_NAME).split(" ");
            var scopes = Arrays.stream(scopeClaims).map(SecurityScope::valueOf).collect(Collectors.toSet());

            return new SecurityUser(id, jwt.getSubject(), "not available", scopes);
        }

        throw new AccessDeniedException("Invalid authentication method");
    }

}
