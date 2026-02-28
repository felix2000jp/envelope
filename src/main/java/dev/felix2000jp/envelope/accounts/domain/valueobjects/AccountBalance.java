package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.jmolecules.ddd.types.ValueObject;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Embeddable
public record AccountBalance(BigDecimal value) implements ValueObject {

    public AccountBalance {
        Assert.notNull(value, "value cannot be null");
    }

}
