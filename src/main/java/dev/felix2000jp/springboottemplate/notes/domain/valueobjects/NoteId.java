package dev.felix2000jp.springboottemplate.notes.domain.valueobjects;

import org.jmolecules.ddd.types.Identifier;
import org.springframework.util.Assert;

import java.util.UUID;

public record NoteId(UUID appuserIdValue, UUID noteIdValue) implements Identifier {

    public NoteId {
        Assert.notNull(appuserIdValue, "NoteId appuser id cannot be null");
        Assert.notNull(noteIdValue, "NoteId note id cannot be null");
    }

}
