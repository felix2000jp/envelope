package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Embeddable
public record TransactionDate(LocalDate value) {

    public TransactionDate {
        Assert.notNull(value, "value must not be null");
    }

}