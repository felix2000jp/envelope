package dev.felix2000jp.springboottemplate.notes.infrastructure.database;

import dev.felix2000jp.springboottemplate.notes.domain.Note;
import dev.felix2000jp.springboottemplate.notes.domain.NoteRepository;
import dev.felix2000jp.springboottemplate.notes.domain.valueobjects.NoteId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class DefaultNoteRepository implements NoteRepository {

    private final NoteJpaRepository noteJpaRepository;

    DefaultNoteRepository(NoteJpaRepository noteJpaRepository) {
        this.noteJpaRepository = noteJpaRepository;
    }

    @Override
    public List<Note> findAllByIdAppuserIdValue(UUID appuserIdValue) {
        return noteJpaRepository.findAllByAppuserId(appuserIdValue);
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        return noteJpaRepository.findByIdAndAppuserId(id.noteIdValue(), id.appuserIdValue());
    }

    @Override
    public void delete(Note note) {
        noteJpaRepository.delete(note);
    }

    @Override
    public void deleteAll() {
        noteJpaRepository.deleteAll();
    }

    @Override
    public void deleteAllByIdAppuserIdValue(UUID appuserIdValue) {
        noteJpaRepository.deleteAllByAppuserId(appuserIdValue);
    }

    @Override
    public void save(Note note) {
        noteJpaRepository.save(note);
    }
}
