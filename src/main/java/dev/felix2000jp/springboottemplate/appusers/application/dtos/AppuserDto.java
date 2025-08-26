package dev.felix2000jp.springboottemplate.appusers.application.dtos;

import java.util.List;
import java.util.UUID;

public record AppuserDto(UUID id, String username, List<String> scopes) {
}
