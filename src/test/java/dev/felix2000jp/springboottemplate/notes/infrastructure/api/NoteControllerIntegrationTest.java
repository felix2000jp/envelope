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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import({TestcontainersConfiguration.class})
class NoteControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private NoteRepository noteRepository;

    private Note note;
    private String token;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();

        note = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title"),
                new Content("content")
        );
        noteRepository.save(note);

        token = securityService.generateToken(
                note.getId().appuserIdValue(),
                "username",
                List.of(SecurityScope.APPLICATION)
        );
    }

    @Test
    void get_return_notes() {
        var findNoteBydIdEntity = restTestClient
                .get()
                .uri("/api/notes")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(NoteListDto.class)
                .returnResult();

        assertThat(findNoteBydIdEntity.getResponseBody()).isNotNull();
        assertThat(findNoteBydIdEntity.getResponseBody().notes()).hasSize(1);
    }

    @Test
    void getByNoteIdValue_then_return_note() {
        var findNoteBydIdEntity = restTestClient
                .get()
                .uri("/api/notes/" + note.getId().noteIdValue())
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(NoteDto.class)
                .returnResult();

        assertThat(findNoteBydIdEntity.getResponseBody()).isNotNull();
        assertThat(findNoteBydIdEntity.getResponseBody().title()).isEqualTo(note.getTitle().value());
        assertThat(findNoteBydIdEntity.getResponseBody().content()).isEqualTo(note.getContent().value());
    }

    @Test
    void create_then_create_note() {
        restTestClient
                .post()
                .uri("/api/notes")
                .headers(h -> h.setBearerAuth(token))
                .body(new CreateNoteDto("title", "content"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Void.class);

        var userNotes = noteRepository.findAllByIdAppuserIdValue(note.getId().appuserIdValue());
        assertThat(userNotes).hasSize(2);
    }

    @Test
    void update_then_update_note() {
        restTestClient
                .put()
                .uri("/api/notes/" + note.getId().noteIdValue())
                .headers(h -> h.setBearerAuth(token))
                .body(new UpdateNoteDto("new title", "new content"))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        var updatedNote = noteRepository.findById(note.getId());
        assertThat(updatedNote).isPresent();
        assertThat(updatedNote.get().getTitle().value()).isEqualTo("new title");
        assertThat(updatedNote.get().getContent().value()).isEqualTo("new content");
    }

    @Test
    void deleteByNoteIdValue_then_delete_note() {
        restTestClient
                .delete()
                .uri("/api/notes/" + note.getId().noteIdValue())
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);

        var deletedNote = noteRepository.findById(note.getId());
        assertThat(deletedNote).isNotPresent();
    }

}
