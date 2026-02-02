package dev.felix2000jp.springboottemplate.notes.application.dtos;

import java.util.List;

public record NoteListDto(int total, List<NoteDto> notes) {
}
