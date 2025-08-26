# Spring Boot Template

This project serves as template for new spring applications. It follows a modular monolith architecture enforced with
the spring modulith package as well as a simplified onion architecture enforced with j-molecules. It currently has 3
modules: auth (for auth and user management related stuff), notes (for note related stuff) and shared (for common stuff
like configurations).

Since this is a modular monolith, dependencies between modules are limited. These are only allowed through root level
interfaces/classes (ex: `SecurityService.java`) and named interfaces (ex `AppuserDeletedEvent`). Also, inside modules a
simplified onion architecture is used therefore inner layers must not depend on outer layers (domain -> application ->
infrastructure).

## Authentication

This template has 2 different type of authentication: basic authentication (username and password) and bearer token
authentication (JWT). It also uses sessions for the frontend as they safer and easier to use in this use case

The module `auth` is where everything related to authentication and authorization is. This projects uses the default
configurations for both basic and bearer authentications apart from the fact that it disabled csrf and session state as
they are not necessary.

Also, Jwt authentication type requires a public and private secrets/keys in order to decode and encode the tokens. I
have already generated the keys which are located in **resources/certificates**. To generate new keys follow these
steps:

```
# create rsa key pair
openssl genrsa -out keypair.pem 2048

# extract public key
openssl rsa -in keypair.pem -pubout -out public.pem

# create private key in PKCS#8 format
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
```

## Logs, traces and metrics

This template includes Spring Boot Actuator with both the health and metrics endpoints enabled.

For storing and viewing logs, traces and metrics this template uses the Grafana LGTM stack with an OpenTelemetry
Collector. As for capturing telemetry data, the OpenTelemetry Java Agent is configured to run alongside the application
and publish the data to the collector via grpc.

There is also the `RequestIdHeaderFilter` which creates a unique id for every http request and adds this id to both the
response headers and the MDC. You use this id for correlating logs and requests. Note that this id only tracks the http
request and therefore cannot be used for tracking the entire trace, for that purpose use the trace_id attribute.

The application uses the open telemetry java agent. To download it
go [here](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases).

## GitHub's configuration, actions and applications

This GitHub repository is a template repository. It is intended to be used as a start of point for new projects. As such
it has some minimal configurations that can be extended further if needed.

### GitHub configuration

#### Merge rules

To protect both the main branch and the git history a couple of rules are in place:

1. Only squash merges are allowed to the main branch
2. Before merging changes to the main branch a pull request must be opened
3. Before merging changes to the main branch all status checks must pass.

#### Dependabot

To help with keeping the application dependencies always up to date, this repository has dependabot configured to, on a
weekly basis open PRs with version updates for both the maven and actions dependencies used.

### GitHub actions - CI/CD Workflow

This workflow is made of 4 different jobs and its intent is to scan the code for vulnerabilities, build and test your
changes and then to build a docker image with the resulting JAR file and push it to docker hub, finally it will deploy
your image.

#### Build and test the code

This is the **first** job in this workflow.

It sets up temurin JDK and maven and then runs the command "mvn clean verify". This command will compile the code, test
it, package it in a JAR file and run code analysis performed by sonar. It then uploads the resulting JAR file, so it can
be used in other jobs.

#### Build and push the image

This job is dependent on [Build and test the code](#build-and-test-the-code).

It downloads the JAR file artifact and build a docker image with it. After building the image it pushes it to the
digital ocean registry. This step is configured to only run it is running on the main branch.

#### Deploy the Application

This job is dependent on [Build and push the image](#build-and-push-the-image).

In this step we should make the necessary changes to deploy the application to wherever it should be deployed.

### GitHub applications - Semantic PR

It verifies your Pull Request title follows the conventional commit guidelines. For more information on the rules being
enforced, take a look at
the [angular commit message guidelines](https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines).

- feat: A new feature
- fix: A bug fix
- docs: Documentation only changes
- style: Changes that do not affect the meaning of the code (white-space, formatting, missing semicolons, etc...)
- refactor: A code change that neither fixes a bug nor adds a feature
- perf: A code change that improves performance
- test: Adding missing tests or correcting existing tests
- build: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
- ci: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
- chore: Changes that don't modify the source code directly but are important for maintaining the project
- revert: Revert existing code

### GitHub applications - SonarCloud Code Analysis

This application will look at your linting results from [Sonar Cloud](https://sonarcloud.io) and block any merge that
introduces new issues, be
it bugs, vulnerabilities, technical debt, decreased coverage or an increased code duplication.

### GitHub Security - Trivy Scan Results

GitHub code scanning results are configured to read form the trivy scan results. Merges are blocked if results are found
with medium or higher severity as well as any errors or warnings.