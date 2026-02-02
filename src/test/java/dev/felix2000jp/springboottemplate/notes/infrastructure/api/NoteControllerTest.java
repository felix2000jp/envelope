package dev.felix2000jp.springboottemplate.notes.infrastructure.api;

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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NoteController.class)
class NoteControllerTest {

    @MockitoBean
    private NoteService noteService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @WithMockUser
    void get_then_return_200_and_notes() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var noteListDto = new NoteListDto(1, List.of(noteDto));

        var expectedResponse = jsonMapper.writeValueAsString(noteListDto);

        when(noteService.get()).thenReturn(noteListDto);

        var request = get("/api/notes");
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @WithMockUser
    void getByNoteIdValue_given_id_then_return_200_and_note() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var expectedResponse = jsonMapper.writeValueAsString(noteDto);

        when(noteService.getByNoteIdValue(noteDto.id())).thenReturn(noteDto);

        var request = get("/api/notes/" + noteDto.id());
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @WithMockUser
    void getByNoteIdValue_given_not_found_id_then_return_404() throws Exception {
        var exception = new NoteNotFoundException();
        when(noteService.getByNoteIdValue(any())).thenThrow(exception);

        var request = get("/api/notes/" + UUID.randomUUID());
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void create_then_return_201_and_location_header() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var createNoteDto = new CreateNoteDto(noteDto.title(), noteDto.content());

        var body = jsonMapper.writeValueAsString(createNoteDto);

        var request = post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("LOCATION", "/api/notes"));
    }

    @ParameterizedTest
    @MethodSource
    @WithMockUser
    void create_given_invalid_body_then_return_404(String title, String content) throws Exception {
        var createNoteDto = new CreateNoteDto(title, content);
        var body = jsonMapper.writeValueAsString(createNoteDto);
        var request = post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> create_given_invalid_body_then_return_404() {
        return Stream.of(
                arguments(null, "content"),
                arguments("", "content"),
                arguments(" ", "content"),
                arguments("title", null),
                arguments("title", ""),
                arguments("title", " ")
        );
    }

    @Test
    @WithMockUser
    void update_then_return_204() throws Exception {
        var noteDto = new NoteDto(UUID.randomUUID(), "title", "content");
        var updateNoteDto = new UpdateNoteDto(noteDto.title(), noteDto.content());

        var body = jsonMapper.writeValueAsString(updateNoteDto);

        var request = put("/api/notes/" + noteDto.id()).contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void update_given_not_found_id_then_return_404() throws Exception {
        var id = UUID.randomUUID();
        var updateNoteDto = new UpdateNoteDto("title", "content");

        var body = jsonMapper.writeValueAsString(updateNoteDto);

        var exception = new NoteNotFoundException();
        doThrow(exception).when(noteService).update(id, updateNoteDto);

        var request = put("/api/notes/" + id).contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @MethodSource
    @WithMockUser
    void update_given_invalid_body_then_return_400(String title, String content) throws Exception {
        var updateNoteDto = new UpdateNoteDto(title, content);
        var body = jsonMapper.writeValueAsString(updateNoteDto);
        var request = put("/api/notes/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> update_given_invalid_body_then_return_400() {
        return Stream.of(
                arguments(null, "content"),
                arguments("", "content"),
                arguments(" ", "content"),
                arguments("title", null),
                arguments("title", ""),
                arguments("title", " ")
        );
    }

    @Test
    @WithMockUser
    void deleteByNoteIdValue_given_id_then_return_204() throws Exception {
        var request = delete("/api/notes/" + UUID.randomUUID());
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteByNoteIdValue_given_not_found_id_then_return_404() throws Exception {
        var id = UUID.randomUUID();

        var exception = new NoteNotFoundException();
        doThrow(exception).when(noteService).deleteByNoteIdValue(id);

        var request = delete("/api/notes/" + id);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

}
