package dev.felix2000jp.envelope.appusers.infrastructure.api;

import dev.felix2000jp.envelope.appusers.application.AppuserService;
import dev.felix2000jp.envelope.appusers.application.dtos.AppuserDto;
import dev.felix2000jp.envelope.appusers.application.dtos.CreateAppuserDto;
import dev.felix2000jp.envelope.appusers.application.dtos.UpdateAppuserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/appusers")
class AppuserController {

    private final AppuserService appuserService;

    AppuserController(AppuserService appuserService) {
        this.appuserService = appuserService;
    }

    @GetMapping
    ResponseEntity<AppuserDto> get() {
        var body = appuserService.get();
        return ResponseEntity.ok(body);
    }

    @PostMapping
    ResponseEntity<Void> create(@Valid @RequestBody CreateAppuserDto createAppuserDto) {
        appuserService.create(createAppuserDto);
        var location = URI.create("/api/appusers");
        return ResponseEntity.created(location).build();
    }

    @PutMapping
    ResponseEntity<Void> update(@Valid @RequestBody UpdateAppuserDto updateAppuserDto) {
        appuserService.update(updateAppuserDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    ResponseEntity<Void> delete() {
        appuserService.delete();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    ResponseEntity<String> login() {
        var body = appuserService.login();
        return ResponseEntity.ok(body);
    }

}
