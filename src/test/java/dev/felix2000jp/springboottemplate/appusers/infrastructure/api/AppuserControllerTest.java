package dev.felix2000jp.springboottemplate.appusers.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class, controllers = AppuserController.class)
class AppuserControllerTest {

    @MockitoBean
    private AppuserService appuserService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void get_then_return_200_and_appuser() throws Exception {
        var appuserDto = new AppuserDto(UUID.randomUUID(), "username", List.of("APPLICATION"));
        var expectedResponse = objectMapper.writeValueAsString(appuserDto);

        when(appuserService.get()).thenReturn(appuserDto);

        var request = get("/api/appusers");
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void get_given_not_found_security_user_then_return_404() throws Exception {
        var exception = new AppuserNotFoundException();
        when(appuserService.get()).thenThrow(exception);

        var request = get("/api/appusers");
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_then_return_201_and_location_header() throws Exception {
        var createAppuserDto = new CreateAppuserDto("username", "password");

        var body = objectMapper.writeValueAsString(createAppuserDto);

        var request = post("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("LOCATION", "/api/appusers"));
    }

    @Test
    void create_given_duplicate_username_then_return_409() throws Exception {
        var createAppuserDto = new CreateAppuserDto("username", "password");

        var body = objectMapper.writeValueAsString(createAppuserDto);

        var exception = new AppuserAlreadyExistsException();
        doThrow(exception).when(appuserService).create(createAppuserDto);

        var request = post("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @MethodSource
    void create_given_invalid_body_then_return_400(String body) throws Exception {
        var request = post("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> create_given_invalid_body_then_return_400() {
        return Stream.of(
                arguments(""),
                arguments("{ 'username': 'username' }"),
                arguments("{ 'password': 'password' }"),
                arguments("{ 'username': null, 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': null }"),
                arguments("{ 'username': '', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': '' }"),
                arguments("{ 'username': ' ', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': ' ' }"),
                arguments("{ 'username': 'lol', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': 'lol' }"),
                arguments("{ 'username': '" + "a".repeat(256) + "', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': '" + "a".repeat(256) + "' }")
        );
    }

    @Test
    void update_then_return_204() throws Exception {
        var updateAppuserDto = new UpdateAppuserDto("new username", "new password");

        var body = objectMapper.writeValueAsString(updateAppuserDto);

        var request = put("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void update_given_duplicate_username_then_return_409() throws Exception {
        var updateAppuserDto = new UpdateAppuserDto("new username", "new password");

        var body = objectMapper.writeValueAsString(updateAppuserDto);

        var exception = new AppuserAlreadyExistsException();
        doThrow(exception).when(appuserService).update(updateAppuserDto);

        var request = put("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @MethodSource
    void update_given_invalid_body_then_return_400(String body) throws Exception {
        var request = put("/api/appusers").contentType(MediaType.APPLICATION_JSON).content(body);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    private static Stream<Arguments> update_given_invalid_body_then_return_400() {
        return Stream.of(
                arguments(""),
                arguments("{ 'username': 'username' }"),
                arguments("{ 'password': 'password' }"),
                arguments("{ 'username': null, 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': null }"),
                arguments("{ 'username': '', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': '' }"),
                arguments("{ 'username': ' ', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': ' ' }"),
                arguments("{ 'username': 'lol', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': 'lol' }"),
                arguments("{ 'username': '" + "a".repeat(256) + "', 'password': 'password' }"),
                arguments("{ 'username': 'username', 'password': '" + "a".repeat(256) + "' }")
        );
    }

    @Test
    void delete_then_return_204() throws Exception {
        var request = delete("/api/appusers");
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_given_not_found_security_user_then_return_404() throws Exception {
        var exception = new AppuserNotFoundException();
        doThrow(exception).when(appuserService).delete();

        var request = delete("/api/appusers");
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

}