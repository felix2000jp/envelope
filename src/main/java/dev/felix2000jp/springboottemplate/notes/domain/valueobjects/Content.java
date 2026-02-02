package dev.felix2000jp.springboottemplate.notes.domain.valueobjects;

import org.springframework.util.Assert;

public record Content(String value) {

    public Content {
        Assert.notNull(value, "Content must not be null!");
    }

}
