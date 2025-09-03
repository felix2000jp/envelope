package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Embeddable
public record TransactionAmount(BigDecimal value) {

    public TransactionAmount {
        Assert.notNull(value, "value cannot be null");
    }

}
