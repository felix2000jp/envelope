package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.util.Assert;

import java.util.UUID;

@Embeddable
public record TransactionId(UUID value) implements Identifier {

    public TransactionId {
        Assert.notNull(value, "value cannot be null");
    }

}
