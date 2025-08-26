package dev.felix2000jp.springboottemplate.notes.infrastructure.database;

import dev.felix2000jp.springboottemplate.TestcontainersConfiguration;
import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, DefaultNoteRepository.class})
class DefaultNoteRepositoryIntegrationTest {

    @Autowired
    private DefaultNoteRepository noteRepository;

    private Note note;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();

        note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        noteRepository.save(note);
    }

    @Test
    void findAllByIdAppuserIdValue_given_appuser_id_then_return_notes() {
        var actual = noteRepository.findAllByIdAppuserIdValue(note.getId().appuserIdValue());

        assertThat(actual).isNotEmpty();
    }

    @Test
    void findAllByAppuserId_given_appuser_id_without_notes_then_return_empty_list() {
        var actual = noteRepository.findAllByIdAppuserIdValue(UUID.randomUUID());

        assertThat(actual).isEmpty();
    }

    @Test
    void findById_given_id_then_return_note() {
        var actual = noteRepository.findById(note.getId());

        assertThat(actual).isPresent();
    }

    @Test
    void findById_given_not_found_id_then_return_empty_optional() {
        var actual = noteRepository.findById(new NoteId(UUID.randomUUID(), UUID.randomUUID()));

        assertThat(actual).isNotPresent();
    }

    @Test
    void deleteById_given_note_then_delete_note() {
        noteRepository.delete(note);

        var actual = noteRepository.findById(note.getId());
        assertThat(actual).isNotPresent();
    }

    @Test
    void deleteAll_then_delete_all_notes() {
        noteRepository.deleteAll();

        var actual = noteRepository.findById(note.getId());
        assertThat(actual).isNotPresent();
    }

    @Test
    void deleteAllByIdAppuserIdValue_then_delete_all_notes() {
        noteRepository.deleteAllByIdAppuserIdValue(note.getId().appuserIdValue());

        var actual = noteRepository.findById(note.getId());
        assertThat(actual).isNotPresent();
    }

    @Test
    void save_given_note_then_save_note() {
        var noteToCreate = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title 1"),
                new Content("content 1")
        );
        noteRepository.save(noteToCreate);

        var createdNote = noteRepository.findById(noteToCreate.getId());
        assertThat(createdNote).isPresent();
    }

}
