package dev.felix2000jp.springboottemplate.notes.domain;

import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoteTest {

    @Test
    void from_given_valid_parameters_then_create_note() {
        var note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );

        assertThat(note.getTitle().value()).isEqualTo("title");
        assertThat(note.getContent().value()).isEqualTo("content");
    }

    @Test
    void setTitle_given_valid_title_then_update_title() {
        var note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        var newTitle = new Title("new title");

        note.setTitle(newTitle);

        assertThat(note.getTitle().value()).isEqualTo("new title");
    }

    @Test
    void setContent_given_new_content_then_update_content() {
        var note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        var newContent = new Content("new content");

        note.setContent(newContent);

        assertThat(note.getContent().value()).isEqualTo("new content");
    }

}
