package dev.felix2000jp.envelope.appusers.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.jmolecules.ddd.types.ValueObject;
import org.springframework.util.Assert;

@Embeddable
public record Scope(String value) implements ValueObject {

    public Scope {
        Assert.notNull(value, "value cannot be null");
    }

}
