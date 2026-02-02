package dev.felix2000jp.springboottemplate.notes.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record UpdateNoteDto(
        @NotBlank
        String title,

        @NotBlank
        String content
) {
}
