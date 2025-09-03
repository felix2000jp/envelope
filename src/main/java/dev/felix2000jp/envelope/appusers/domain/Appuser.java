package dev.felix2000jp.envelope.appusers.domain;

import dev.felix2000jp.envelope.appusers.domain.events.AppuserDeletedEvent;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.AppuserId;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Password;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Scope;
import dev.felix2000jp.envelope.appusers.domain.valueobjects.Username;
import jakarta.persistence.*;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@jakarta.persistence.Table(name = "appuser")
@jakarta.persistence.Entity
public class Appuser implements AggregateRoot<Appuser, AppuserId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private AppuserId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, unique = true))
    private Username username;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false))
    private Password password;

    @ElementCollection(fetch = FetchType.EAGER)
    @AttributeOverride(name = "value", column = @Column(name = "scope", nullable = false))
    private List<Scope> scopes;

    @Transient
    private final Collection<DomainEvent> domainEvents = new ArrayList<>();

    protected Appuser() {
    }

    protected Appuser(AppuserId id, Username username, Password password, List<Scope> scopes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.scopes = scopes;
    }

    public static Appuser from(AppuserId id, Username username, Password password) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(username, "username must not be null");
        Assert.notNull(password, "password must not be null");

        return new Appuser(id, username, password, new ArrayList<>());
    }

    @Override
    public AppuserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public void setUsername(Username username) {
        this.username = username;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void addApplicationScope() {
        var applicationScope = new Scope("APPLICATION");
        scopes.add(applicationScope);
    }

    public void delete() {
        var event = new AppuserDeletedEvent(getId().value());
        domainEvents.add(event);
    }

    @DomainEvents
    Collection<DomainEvent> getDomainEvents() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    void clearDomainEvents() {
        domainEvents.clear();
    }

}
