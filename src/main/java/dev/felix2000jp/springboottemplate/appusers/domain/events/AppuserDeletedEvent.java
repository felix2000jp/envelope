package dev.felix2000jp.springboottemplate.appusers.domain.events;

import org.jmolecules.event.types.DomainEvent;
import org.springframework.modulith.NamedInterface;

import java.util.UUID;

@NamedInterface
public record AppuserDeletedEvent(UUID appuserId) implements DomainEvent {
}
