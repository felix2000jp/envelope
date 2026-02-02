package dev.felix2000jp.springboottemplate.appusers.infrastructure.api;

import dev.felix2000jp.springboottemplate.appusers.application.AppuserService;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.AppuserDto;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.CreateAppuserDto;
import dev.felix2000jp.springboottemplate.appusers.application.dtos.UpdateAppuserDto;
import dev.felix2000jp.springboottemplate.appusers.domain.exceptions.AppuserAlreadyExistsException;
import dev.felix2000jp.springboottemplate.appusers.domain.exceptions.AppuserNotFoundException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppuserController.class)
class AppuserControllerTest {

    @MockitoBean
    private AppuserService appuserService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @WithMockUser
    void get_then_return_200_and_appuser() throws Exception {
        var appuserDto = new AppuserDto(UUID.randomUUID(), "username", List.of("APPLICATION"));
        var expectedResponse = jsonMapper.writeValueAsString(appuserDto);

        when(appuserService.get()).thenReturn(appuserDto);

        var request = get("/api/appusers");
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @WithMockUser
    void get_given_not_found_security_user_then_return_404() throws Exception {
        var exception = new AppuserNotFoundException();
        when(appuserService.get()).thenThrow(exception);

        var request = get("/api/appusers");
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
        var createAppuserDto = new CreateAppuserDto("username", "password");

        var body = jsonMapper.writeValueAsString(createAppuserDto);

        var request = post("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("LOCATION", "/api/appusers"));
    }

    @Test
    @WithMockUser
    void create_given_duplicate_username_then_return_409() throws Exception {
        var createAppuserDto = new CreateAppuserDto("username", "password");

        var body = jsonMapper.writeValueAsString(createAppuserDto);

        var exception = new AppuserAlreadyExistsException();
        doThrow(exception).when(appuserService).create(createAppuserDto);

        var request = post("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @MethodSource
    @WithMockUser
    void create_given_invalid_body_then_return_400(String username, String password) throws Exception {
        var createAppuserDto = new CreateAppuserDto(username, password);
        var body = jsonMapper.writeValueAsString(createAppuserDto);
        var request = post("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> create_given_invalid_body_then_return_400() {
        return Stream.of(
                arguments(null, "password"),
                arguments("", "password"),
                arguments(" ", "password"),
                arguments("a", "password"),
                arguments("a".repeat(256), "password"),
                arguments("username", null),
                arguments("username", ""),
                arguments("username", " "),
                arguments("username", "a"),
                arguments("username", "a".repeat(256))
        );
    }

    @Test
    @WithMockUser
    void update_then_return_204() throws Exception {
        var updateAppuserDto = new UpdateAppuserDto("new username", "new password");

        var body = jsonMapper.writeValueAsString(updateAppuserDto);

        var request = put("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void update_given_duplicate_username_then_return_409() throws Exception {
        var updateAppuserDto = new UpdateAppuserDto("new username", "new password");

        var body = jsonMapper.writeValueAsString(updateAppuserDto);

        var exception = new AppuserAlreadyExistsException();
        doThrow(exception).when(appuserService).update(updateAppuserDto);

        var request = put("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @MethodSource
    @WithMockUser
    void update_given_invalid_body_then_return_400(String username, String password) throws Exception {
        var updateAppuserDto = new UpdateAppuserDto(username, password);
        var body = jsonMapper.writeValueAsString(updateAppuserDto);
        var request = put("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> update_given_invalid_body_then_return_400() {
        return Stream.of(
                arguments(null, "password"),
                arguments("", "password"),
                arguments(" ", "password"),
                arguments("a", "password"),
                arguments("a".repeat(256), "password"),
                arguments("username", null),
                arguments("username", ""),
                arguments("username", " "),
                arguments("username", "a"),
                arguments("username", "a".repeat(256))
        );
    }

    @Test
    @WithMockUser
    void delete_then_return_204() throws Exception {
        var request = delete("/api/appusers");
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_given_not_found_security_user_then_return_404() throws Exception {
        var exception = new AppuserNotFoundException();
        doThrow(exception).when(appuserService).delete();

        var request = delete("/api/appusers");
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void login_then_return_200_and_token() throws Exception {
        var appuserToken = "sample-jwt-token";

        when(appuserService.login()).thenReturn(appuserToken);

        var request = post("/api/appusers/login");
        mockMvc
                .perform(request.with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(appuserToken));
    }

}