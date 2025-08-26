package dev.felix2000jp.springboottemplate.notes.domain;

import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.util.Assert;

import java.util.UUID;

@jakarta.persistence.Table(name = "note")
@jakarta.persistence.Entity
public class Note implements AggregateRoot<Note, NoteId> {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "appuser_id")
    private UUID appuserId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    protected Note() {
    }

    protected Note(UUID id, UUID appuserId, String title, String content) {
        this.id = id;
        this.appuserId = appuserId;
        this.title = title;
        this.content = content;
    }

    public static Note from(NoteId id, Title title, Content content) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(title, "title must not be null");
        Assert.notNull(content, "content must not be null");

        return new Note(id.noteIdValue(), id.appuserIdValue(), title.value(), content.value());
    }

    @Override
    public NoteId getId() {
        return new NoteId(appuserId, id);
    }

    public Title getTitle() {
        return new Title(title);
    }

    public void setTitle(Title title) {
        this.title = title.value();
    }

    public Content getContent() {
        return new Content(content);
    }

    public void setContent(Content content) {
        this.content = content.value();
    }
}