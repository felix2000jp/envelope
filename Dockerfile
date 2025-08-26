FROM eclipse-temurin:21

RUN groupadd spring && useradd -m -g spring spring

ARG OTEL_VERSION=2.10.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar \
    opentelemetry-javaagent.jar

ARG APP_JAR=target/*.jar
COPY ${APP_JAR} app.jar

USER spring:spring

ENTRYPOINT [ "java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "app.jar" ]