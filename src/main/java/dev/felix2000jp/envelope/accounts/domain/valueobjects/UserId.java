package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

import java.util.UUID;

@Embeddable
public record UserId(UUID value) {

    public UserId {
        Assert.notNull(value, "value cannot be null");
    }

}
