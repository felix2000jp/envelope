package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.jmolecules.ddd.types.ValueObject;
import org.springframework.util.Assert;

@Embeddable
public record TransactionMemo(String value) implements ValueObject {

    public TransactionMemo {
        Assert.notNull(value, "value cannot be null");
    }

}
