package dev.felix2000jp.springboottemplate.notes.application.handlers;

import dev.felix2000jp.springboottemplate.TestcontainersConfiguration;
import dev.felix2000jp.springboottemplate.appusers.domain.events.AppuserDeletedEvent;
import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.NoteRepository;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
@Import({TestcontainersConfiguration.class})
class AppuserDeletedEventHandlerIntegrationTest {

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private AppuserDeletedEventHandler eventHandler;

    @Test
    void on_given_event_then_delete_all_notes_with_appuser_id_value(Scenario scenario) {
        var appuserId = UUID.randomUUID();
        var appuserDeletedEvent = new AppuserDeletedEvent(appuserId);

        var note1 = Note.from(
                new NoteId(appuserId, UUID.randomUUID()),
                new Title("title 1"),
                new Content("content 1")
        );
        var note2 = Note.from(
                new NoteId(appuserId, UUID.randomUUID()),
                new Title("title 2"),
                new Content("content 2")
        );
        var note3 = Note.from(
                new NoteId(UUID.randomUUID(), UUID.randomUUID()),
                new Title("title 3"),
                new Content("content 3")
        );

        noteRepository.save(note1);
        noteRepository.save(note2);
        noteRepository.save(note3);

        scenario
                .publish(appuserDeletedEvent)
                .andWaitForStateChange(() -> eventHandler)
                .andVerify(unusedEventHandler -> {
                    assertThat(noteRepository.findById(note1.getId())).isNotPresent();
                    assertThat(noteRepository.findById(note2.getId())).isNotPresent();
                    assertThat(noteRepository.findById(note3.getId())).isPresent();
                });
    }

}
