package dev.felix2000jp.springboottemplate.notes.application;

import dev.felix2000jp.springboottemplate.notes.application.dtos.CreateNoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteListDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.UpdateNoteDto;
import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.NoteRepository;
import dev.felix2000jp.springboottemplate.notes.domain.exceptions.NoteNotFoundException;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Content;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.Title;
import dev.felix2000jp.springboottemplate.system.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final SecurityService securityService;

    NoteService(NoteRepository noteRepository, NoteMapper noteMapper, SecurityService securityService) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
        this.securityService = securityService;
    }

    @Transactional(readOnly = true)
    public NoteListDto get() {
        var user = securityService.loadUserFromSecurityContext();
        var notes = noteRepository.findAllByIdAppuserIdValue(user.id());
        return noteMapper.toDto(notes);
    }

    @Transactional(readOnly = true)
    public NoteDto getByNoteIdValue(UUID noteIdValue) {
        var user = securityService.loadUserFromSecurityContext();
        var noteId = new NoteId(user.id(), noteIdValue);

        var note = noteRepository
                .findById(noteId)
                .orElseThrow(NoteNotFoundException::new);

        return noteMapper.toDto(note);
    }

    @Transactional
    public void create(CreateNoteDto createNoteDto) {
        var user = securityService.loadUserFromSecurityContext();

        var noteToCreate = Note.from(
                new NoteId(user.id(), UUID.randomUUID()),
                new Title(createNoteDto.title()),
                new Content(createNoteDto.content())
        );
        noteRepository.save(noteToCreate);
        log.info("Note with id {} created", noteToCreate.getId());
    }

    @Transactional
    public void update(UUID noteIdValue, UpdateNoteDto updateNoteDto) {
        var user = securityService.loadUserFromSecurityContext();
        var noteId = new NoteId(user.id(), noteIdValue);

        var noteToUpdate = noteRepository
                .findById(noteId)
                .orElseThrow(NoteNotFoundException::new);

        noteToUpdate.setTitle(new Title(updateNoteDto.title()));
        noteToUpdate.setContent(new Content(updateNoteDto.content()));
        noteRepository.save(noteToUpdate);
        log.info("Note with id {} updated", noteToUpdate.getId());
    }

    @Transactional
    public void deleteByNoteIdValue(UUID noteIdValue) {
        var user = securityService.loadUserFromSecurityContext();
        var noteId = new NoteId(user.id(), noteIdValue);

        var noteToDelete = noteRepository
                .findById(noteId)
                .orElseThrow(NoteNotFoundException::new);

        noteRepository.delete(noteToDelete);
        log.info("Note with id {} deleted", noteToDelete.getId());
    }

    @Transactional
    public void deleteAllByAppuserIdValue(UUID appuserIdValue) {
        noteRepository.deleteAllByIdAppuserIdValue(appuserIdValue);
        log.info("Notes with appuserId {} deleted", appuserIdValue);
    }

}
