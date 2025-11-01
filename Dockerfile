FROM eclipse-temurin:25
RUN groupadd spring && useradd -m -g spring spring

# Uncomment for ready to use development builds
# COPY certs /certs
# COPY otel/*.jar /otel.jar

ARG TARGET_JAR=target/*.jar
COPY ${TARGET_JAR} /target.jar

# To use in prod run the command:
# docker run -v /path-to-certs:/certs:ro -v /path-to-otel.jar:/otel.jar:ro spring-app:latest
USER spring:spring
ENTRYPOINT ["java", "-javaagent:/otel.jar", "-jar", "/target.jar"]