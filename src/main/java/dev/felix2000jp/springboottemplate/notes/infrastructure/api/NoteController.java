package dev.felix2000jp.springboottemplate.notes.infrastructure.api;

import dev.felix2000jp.springboottemplate.notes.application.NoteService;
import dev.felix2000jp.springboottemplate.notes.application.dtos.CreateNoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteListDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.UpdateNoteDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/notes")
class NoteController {

    private final NoteService noteService;

    NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    ResponseEntity<NoteListDto> get() {
        var body = noteService.get();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{noteIdValue}")
    ResponseEntity<NoteDto> getByNoteIdValue(@PathVariable UUID noteIdValue) {
        var body = noteService.getByNoteIdValue(noteIdValue);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    ResponseEntity<NoteDto> create(@RequestBody @Valid CreateNoteDto createNoteDto) {
        noteService.create(createNoteDto);
        var location = URI.create("/api/notes");
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{noteIdValue}")
    ResponseEntity<Void> update(@PathVariable UUID noteIdValue, @RequestBody @Valid UpdateNoteDto updateNoteDto) {
        noteService.update(noteIdValue, updateNoteDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{noteIdValue}")
    ResponseEntity<Void> deleteByNoteIdValue(@PathVariable UUID noteIdValue) {
        noteService.deleteByNoteIdValue(noteIdValue);
        return ResponseEntity.noContent().build();
    }

}
