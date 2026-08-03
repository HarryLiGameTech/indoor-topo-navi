FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY toponavi-core ./toponavi-core
COPY toponavi-dsl ./toponavi-dsl
COPY toponavi-web ./toponavi-web

RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon :toponavi-web:bootJar

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 toponavi \
    && useradd --system --uid 10001 --gid toponavi --create-home toponavi

WORKDIR /app

COPY --from=builder --chown=10001:10001 \
    /workspace/toponavi-web/build/libs/toponavi-web.jar \
    /app/toponavi-web.jar

ENV SERVER_PORT=8080 \
    PLATFORM_EXAMPLES_PATH=/maps \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

USER 10001:10001

HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=5 \
    CMD curl --fail --silent --show-error http://127.0.0.1:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/toponavi-web.jar"]
