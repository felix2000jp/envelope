package dev.felix2000jp.envelope.notes.domain.exceptions;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException() {
        super("Note could not be found");
    }

}
