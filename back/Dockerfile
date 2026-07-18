# Multi-stage build: compile with the Gradle wrapper, ship only the JRE + jar.

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew --version

COPY src src
COPY config config

RUN ./gradlew bootJar -x test -x checkstyleMain -x checkstyleTest -x spotbugsMain --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system recadero && adduser --system --ingroup recadero recadero
COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown recadero:recadero app.jar
USER recadero

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/api/v1/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
