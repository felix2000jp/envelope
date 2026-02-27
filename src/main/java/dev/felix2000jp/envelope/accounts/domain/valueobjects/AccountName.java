package dev.felix2000jp.envelope.accounts.domain.valueobjects;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public record AccountName(String value) {

    public AccountName {
        Assert.hasText(value, "value cannot be null or empty or blank");
    }

}
