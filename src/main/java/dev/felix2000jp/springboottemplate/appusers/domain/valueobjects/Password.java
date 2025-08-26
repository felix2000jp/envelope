package dev.felix2000jp.springboottemplate.appusers.domain.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jmolecules.ddd.types.ValueObject;
import org.springframework.util.Assert;

@Embeddable
public record Password(
        @Column(name = "password")
        String value
) implements ValueObject {

    public Password {
        Assert.notNull(value, "value cannot be null");
        Assert.isTrue(value.length() >= 8, "value must be at least 8 characters");
        Assert.isTrue(value.length() <= 255, "value must be at max 255 characters");
    }

}
