package dev.felix2000jp.springboottemplate.notes.application;

import dev.felix2000jp.springboottemplate.notes.application.dtos.CreateNoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.UpdateNoteDto;
import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.NoteRepository;
import dev.felix2000jp.springboottemplate.notes.domain.exceptions.NoteNotFoundException;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import dev.felix2000jp.springboottemplate.system.security.SecurityScope;
import dev.felix2000jp.springboottemplate.system.security.SecurityService;
import dev.felix2000jp.springboottemplate.system.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;
    @Spy
    private NoteMapper noteMapper;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private NoteService noteService;

    @Captor
    private ArgumentCaptor<Note> noteCaptor;

    private SecurityUser user;

    @BeforeEach
    void setUp() {
        user = new SecurityUser(UUID.randomUUID(), "username", "password", Set.of(SecurityScope.APPLICATION));

        when(securityService.loadUserFromSecurityContext()).thenReturn(user);
    }

    @Test
    void get_given_user_then_return_list_of_notes() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );

        when(noteRepository.findAllByIdAppuserIdValue(user.id())).thenReturn(List.of(note));

        var actual = noteService.get();
        var actualNote = actual.notes().getFirst();

        assertThat(actual.notes()).hasSize(1);
        assertThat(actualNote.id()).isEqualTo(note.getId().noteIdValue());
        assertThat(actualNote.title()).isEqualTo(note.getTitle().value());
        assertThat(actualNote.content()).isEqualTo(note.getContent().value());
    }

    @Test
    void get_given_user_without_notes_then_return_empty_list_of_notes() {
        when(noteRepository.findAllByIdAppuserIdValue(user.id())).thenReturn(List.of());

        var actual = noteService.get();

        assertThat(actual.notes()).isEmpty();
    }

    @Test
    void getByNoteIdValue_given_note_id_then_return_note() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );

        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        var actual = noteService.getByNoteIdValue(note.getId().noteIdValue());

        assertThat(actual.id()).isEqualTo(note.getId().noteIdValue());
        assertThat(actual.title()).isEqualTo(note.getTitle().value());
        assertThat(actual.content()).isEqualTo(note.getContent().value());
    }

    @Test
    void getByNoteIdValue_given_not_found_id_then_throw_exception() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        var noteIdValue = note.getId().noteIdValue();

        when(noteRepository.findById(note.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> noteService.getByNoteIdValue(noteIdValue)
        ).isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void create_given_dto_then_create_note() {
        var createNoteDto = new CreateNoteDto("title", "content");

        noteService.create(createNoteDto);

        verify(noteRepository).save(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getTitle().value()).isEqualTo(createNoteDto.title());
        assertThat(noteCaptor.getValue().getContent().value()).isEqualTo(createNoteDto.content());
    }

    @Test
    void update_given_id_and_dto_then_update_note() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        var updateNoteDto = new UpdateNoteDto("new title", "new content");

        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        noteService.update(note.getId().noteIdValue(), updateNoteDto);

        verify(noteRepository).save(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getTitle().value()).isEqualTo(updateNoteDto.title());
        assertThat(noteCaptor.getValue().getContent().value()).isEqualTo(updateNoteDto.content());
    }

    @Test
    void update_given_not_found_id_and_dto_then_exception() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        var noteIdValue = note.getId().noteIdValue();
        var updateNoteDto = new UpdateNoteDto("new title", "new content");

        when(noteRepository.findById(note.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> noteService.update(noteIdValue, updateNoteDto)
        ).isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    void deleteByNoteIdValue_given_note_id_then_delete_note() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );

        when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

        noteService.deleteByNoteIdValue(note.getId().noteIdValue());

        verify(noteRepository).delete(noteCaptor.capture());
        assertThat(noteCaptor.getValue()).isEqualTo(note);
    }

    @Test
    void deleteByNoteIdValue_given_not_found_id_then_throw_exception() {
        var note = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        var noteIdValue = note.getId().noteIdValue();

        when(noteRepository.findById(note.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> noteService.deleteByNoteIdValue(noteIdValue)
        ).isInstanceOf(NoteNotFoundException.class);
    }

}
