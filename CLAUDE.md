# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## IMPORTANT

ALWAYS refer to the appusers module for best practices and code style, naming and architecture conventions. Every new
feature must come with unit tests. When running tests NEVER RUN ALL TESTS, run only the tests for the module you are
working while ignoring the integration tests. Only I can run integration tests.

## Project Overview

This is "Envelope", a Spring Boot 3.5.5 budgeting application using Java 21. It follows a modular monolith architecture
enforced with Spring Modulith and a simplified onion architecture enforced with jMolecules.

## Build System

This is a Maven project. Key commands:

- **Build and test**: `./mvnw clean verify` - Compiles, tests, packages JAR, and runs code analysis
- **Run application**: `./mvnw spring-boot:run`

## Architecture

### Modular Monolith Structure

The application uses Spring Modulith with shared "system" module. Current modules:

- `accounts` - Account management and balance tracking
- `appusers` - User management and authentication
- `system` - Shared configurations and security

### Onion Architecture (within modules)

Each module follows simplified onion architecture enforced by jMolecules:

- `domain` - Core business logic (annotated with `@DomainRing`)
- `application` - Use cases and services (annotated with `@ApplicationRing`)
- `infrastructure` - External concerns such as API and database (annotated with `@InfrastructureRing`)

Dependencies flow: domain → application → infrastructure

## Authentication & Security

Two authentication types supported:

- **Basic Auth**: Username/password (backed by BCryptPasswordEncoder)
- **Bearer Token**: JWT using RSA keys in `/certs/` directory

Security scope: `APPLICATION` scope required for most endpoints except POST `/api/appusers`

JWT keys location: `certs/public.pem` and `certs/private.pem`

## Database

- **Database**: PostgreSQL (configured for localhost:5432)
- **Migrations**: Flyway (enabled, located in `src/main/resources/db/migration/`)
- **ORM**: Spring Data JPA with Hibernate (DDL validation mode)

## Development Environment

### Docker Compose

Local development stack via `compose.yaml`:

- PostgreSQL database on port 5432
- Grafana LGTM stack on ports 3000, 4317, 4318 (production profile)

Start with: `docker compose up -d`

### Spring Boot Features

- **DevTools** enabled for hot reload
- **Docker Compose integration** enabled
- **Virtual threads** enabled
- **Actuator** with health endpoint exposed

## Observability

- OpenTelemetry integration with OTLP metrics export to localhost:4318
- Spring Modulith observability for module boundaries
- Micrometer metrics (most disabled by default, only essentials enabled)

## Testing

- **Framework**: JUnit 5 + Spring Boot Test
- **Containers**: Testcontainers with PostgreSQL
- **Architecture Tests**: jMolecules + ArchUnit for architecture compliance
- **Module Tests**: Spring Modulith testing support

## Key Patterns

### Value Objects

Domain uses extensive value objects (e.g., `AccountId`, `Balance`, `Username`, `Password`)

### Event-Driven Communication

Modules communicate via domain events (e.g., `AppuserDeletedEvent` handled by accounts module)

### Repository Pattern

Domain repositories with JPA implementations in infrastructure layer

### Exception Handling

Module-specific exception handlers in infrastructure/api packages