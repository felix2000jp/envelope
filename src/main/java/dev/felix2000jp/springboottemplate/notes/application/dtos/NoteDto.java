package dev.felix2000jp.springboottemplate.notes.application.dtos;

import java.util.UUID;

public record NoteDto(UUID id, String title, String content) {
}
