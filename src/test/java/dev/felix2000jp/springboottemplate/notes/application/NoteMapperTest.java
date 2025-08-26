package dev.felix2000jp.springboottemplate.notes.application;

import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoteMapperTest {

    private final NoteMapper noteMapper = new NoteMapper();

    @Test
    void toDto_given_note_then_map_to_dto() {
        var note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );

        var actual = noteMapper.toDto(note);

        assertThat(actual.id()).isEqualTo(note.getId().noteIdValue());
        assertThat(actual.title()).isEqualTo(note.getTitle().value());
        assertThat(actual.content()).isEqualTo(note.getContent().value());
    }

    @Test
    void toDto_given_list_of_notes_then_map_to_list_dto() {
        var notes = List.of(
                Note.from(
                        new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                        new Title("title"),
                        new Content("content")
                )
        );

        var actual = noteMapper.toDto(notes);

        assertThat(actual.total()).isEqualTo(1);
    }

}
