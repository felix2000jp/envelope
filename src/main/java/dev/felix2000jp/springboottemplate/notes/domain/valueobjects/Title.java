package dev.felix2000jp.springboottemplate.notes.domain.valueobjects;

import org.springframework.util.Assert;

public record Title(String value) {

    public Title {
        Assert.notNull(value, "Title must not be null");
    }

}
