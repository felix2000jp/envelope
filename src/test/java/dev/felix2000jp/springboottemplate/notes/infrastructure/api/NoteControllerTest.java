package dev.felix2000jp.springboottemplate.notes.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.felix2000jp.springboottemplate.notes.application.NoteService;
import dev.felix2000jp.springboottemplate.notes.application.dtos.CreateNoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.NoteListDto;
import dev.felix2000jp.springboottemplate.notes.application.dtos.UpdateNoteDto;
import dev.felix2000jp.springboottemplate.notes.domain.exceptions.NoteNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class, controllers = NoteController.class)
class NoteControllerTest {

    @MockitoBean
    private NoteService noteService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void get_then_return_200_and_notes() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var noteListDto = new NoteListDto(1, List.of(noteDto));

        var expectedResponse = objectMapper.writeValueAsString(noteListDto);

        when(noteService.get()).thenReturn(noteListDto);

        var request = get("/api/notes");
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void getByNoteIdValue_given_id_then_return_200_and_note() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var expectedResponse = objectMapper.writeValueAsString(noteDto);

        when(noteService.getByNoteIdValue(noteDto.id())).thenReturn(noteDto);

        var request = get("/api/notes/" + noteDto.id());
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void getByNoteIdValue_given_not_found_id_then_return_404() throws Exception {
        var exception = new NoteNotFoundException();
        when(noteService.getByNoteIdValue(any())).thenThrow(exception);

        var request = get("/api/notes/" + UUID.randomUUID());
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_then_return_201_and_location_header() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var createNoteDto = new CreateNoteDto(noteDto.title(), noteDto.content());

        var body = objectMapper.writeValueAsString(createNoteDto);

        var request = post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("LOCATION", "/api/notes"));
    }

    @ParameterizedTest
    @MethodSource
    void create_given_invalid_body_then_return_404(String body) throws Exception {
        var request = post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> create_given_invalid_body_then_return_404() {
        return Stream.of(
                arguments(""),
                arguments("{}"),
                arguments("{ 'title': 'title' }"),
                arguments("{ 'content': 'content' }"),
                arguments("{ 'title': null, 'content': 'content' }"),
                arguments("{ 'title': '', 'content': 'content' }"),
                arguments("{ 'title': ' ', 'content': 'content' }"),
                arguments("{ 'title': 'title', 'content': null }"),
                arguments("{ 'title': 'title', 'content': '' }"),
                arguments("{ 'title': 'title', 'content': ' ' }")
        );
    }

    @Test
    void update_then_return_204() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var updateNoteDto = new UpdateNoteDto(noteDto.title(), noteDto.content());

        var body = objectMapper.writeValueAsString(updateNoteDto);

        var request = put("/api/notes/" + noteDto.id()).contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void update_given_not_found_id_then_return_404() throws Exception {
        var id = UUID.randomUUID();
        var updateNoteDto = new UpdateNoteDto("title", "content");

        var body = objectMapper.writeValueAsString(updateNoteDto);

        var exception = new NoteNotFoundException();
        doThrow(exception).when(noteService).update(id, updateNoteDto);

        var request = put("/api/notes/" + id).contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @MethodSource
    void update_given_invalid_body_then_return_400(String body) throws Exception {
        var request = put("/api/notes/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> update_given_invalid_body_then_return_400() {
        return Stream.of(
                arguments(""),
                arguments("{}"),
                arguments("{ 'title': 'title' }"),
                arguments("{ 'content': 'content' }"),
                arguments("{ 'title': null, 'content': 'content' }"),
                arguments("{ 'title': '', 'content': 'content' }"),
                arguments("{ 'title': ' ', 'content': 'content' }"),
                arguments("{ 'title': 'title', 'content': null }"),
                arguments("{ 'title': 'title', 'content': '' }"),
                arguments("{ 'title': 'title', 'content': ' ' }")
        );
    }

    @Test
    void deleteByNoteIdValue_given_id_then_return_204() throws Exception {
        var request = delete("/api/notes/" + UUID.randomUUID());
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteByNoteIdValue_given_not_found_id_then_return_404() throws Exception {
        var id = UUID.randomUUID();

        var exception = new NoteNotFoundException();
        doThrow(exception).when(noteService).deleteByNoteIdValue(id);

        var request = delete("/api/notes/" + id);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

}
