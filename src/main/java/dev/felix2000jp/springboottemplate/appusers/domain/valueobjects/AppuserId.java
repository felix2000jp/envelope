package dev.felix2000jp.springboottemplate.appusers.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.util.Assert;

import java.util.UUID;

@Embeddable
public record AppuserId(
        @Column(name = "id")
        UUID value
) implements Identifier {

    public AppuserId {
        Assert.notNull(value, "value cannot be null");
    }

}

