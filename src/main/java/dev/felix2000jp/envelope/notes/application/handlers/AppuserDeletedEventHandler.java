package dev.felix2000jp.envelope.notes.application.handlers;

import dev.felix2000jp.envelope.appusers.domain.events.AppuserDeletedEvent;
import dev.felix2000jp.envelope.notes.application.NoteService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class AppuserDeletedEventHandler {

    private final NoteService noteService;

    AppuserDeletedEventHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @ApplicationModuleListener
    void on(AppuserDeletedEvent event) {
        noteService.deleteAllByAppuserIdValue(event.appuserId());
    }

}
