package dev.felix2000jp.springboottemplate.notes.application;

import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteListDto;
import dev.felix2000jp.springboottemplate.notes.domain.Note;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class NoteMapper {

    NoteDto toDto(Note note) {
        return new NoteDto(note.getId().noteIdValue(), note.getTitle().value(), note.getContent().value());
    }

    NoteListDto toDto(List<Note> notes) {
        return new NoteListDto(notes.size(), notes.stream().map(this::toDto).toList());
    }

}
