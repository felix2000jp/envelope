package dev.felix2000jp.springboottemplate.notes.infrastructure.api;

import dev.felix2000jp.springboottemplate.TestcontainersConfiguration;
import dev.felix2000jp.springboottemplate.notes.application.dtos.CreateNoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteListDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.UpdateNoteDto;
import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.NoteRepository;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import dev.felix2000jp.springboottemplate.system.security.SecurityScope;
import dev.felix2000jp.springboottemplate.system.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class})
class NoteControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private NoteRepository noteRepository;

    private Note note;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();

        note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        noteRepository.save(note);

        var token = securityService.generateToken(
                note.getId().appuserIdValue(),
                "username",
                List.of(SecurityScope.APPLICATION)
        );

        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
    }

    @Test
    void get_return_notes() {
        var findNoteBydIdEntity = testRestTemplate.exchange(
                "/api/notes",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                NoteListDto.class
        );

        assertThat(findNoteBydIdEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(findNoteBydIdEntity.getBody()).isNotNull();
        assertThat(findNoteBydIdEntity.getBody().notes()).hasSize(1);
    }

    @Test
    void getByNoteIdValue_then_return_note() {
        var findNoteBydIdEntity = testRestTemplate.exchange(
                "/api/notes/{id}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                NoteDto.class,
                note.getId().noteIdValue()
        );

        assertThat(findNoteBydIdEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(findNoteBydIdEntity.getBody()).isNotNull();
        assertThat(findNoteBydIdEntity.getBody().title()).isEqualTo(note.getTitle().value());
        assertThat(findNoteBydIdEntity.getBody().content()).isEqualTo(note.getContent().value());
    }

    @Test
    void create_then_create_note() {
        var createNoteEntity = testRestTemplate.exchange(
                "/api/notes",
                HttpMethod.POST,
                new HttpEntity<>(new CreateNoteDto("title", "content"), headers),
                Void.class
        );

        assertThat(createNoteEntity.getStatusCode().value()).isEqualTo(201);

        var userNotes = noteRepository.findAllByIdAppuserIdValue(note.getId().appuserIdValue());
        assertThat(userNotes).hasSize(2);
    }

    @Test
    void update_then_update_note() {
        var updateNoteEntity = testRestTemplate.exchange(
                "/api/notes/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateNoteDto("new title", "new content"), headers),
                Void.class,
                note.getId().noteIdValue()
        );

        assertThat(updateNoteEntity.getStatusCode().value()).isEqualTo(204);

        var updatedNote = noteRepository.findById(note.getId());
        assertThat(updatedNote).isPresent();
        assertThat(updatedNote.get().getTitle().value()).isEqualTo("new title");
        assertThat(updatedNote.get().getContent().value()).isEqualTo("new content");
    }

    @Test
    void deleteByNoteIdValue_then_delete_note() {
        var deleteNoteEntity = testRestTemplate.exchange(
                "/api/notes/{id}",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class,
                note.getId().noteIdValue()
        );

        assertThat(deleteNoteEntity.getStatusCode().value()).isEqualTo(204);

        var deletedNote = noteRepository.findById(note.getId());
        assertThat(deletedNote).isNotPresent();
    }

}
