package com.e611.toponavi.web.project;

import com.e611.toponavi.web.cache.CompilationCacheService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared PostgreSQL 16 for integration tests. Starting it requires Docker; without Docker the tests fail rather
 * than being skipped, because persistence behavior cannot be verified any other way.
 */
public final class PostgresIntegrationSupport {
    public static final String TOKEN = "integration-service-token_0123456789-ABCDEFGH";

    private static PostgreSQLContainer<?> shared;

    private PostgresIntegrationSupport() {
    }

    public static PostgreSQLContainer<?> newContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                .withDatabaseName("toponavi").withUsername("toponavi").withPassword("toponavi")
                .withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF8");
        container.start();
        createLegacySentinels(container);
        return container;
    }

    public static synchronized PostgreSQLContainer<?> shared() {
        if (shared == null) shared = newContainer();
        return shared;
    }

    public static void register(DynamicPropertyRegistry registry, PostgreSQLContainer<?> container) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("platform.security.service-token", () -> TOKEN);
    }

    public static Connection connect(PostgreSQLContainer<?> container) throws SQLException {
        return DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    }

    /** A pre-existing legacy platform schema that the project-store migration must leave untouched. */
    private static void createLegacySentinels(PostgreSQLContainer<?> container) {
        try (Connection connection = connect(container); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE public.maps (id uuid PRIMARY KEY, name text NOT NULL, repo text)");
            statement.execute("INSERT INTO public.maps VALUES ('00000000-0000-0000-0000-000000000001', 'legacy sentinel', 'toponavi-map-store/legacy')");
            statement.execute("CREATE TABLE public.users (id uuid PRIMARY KEY, github_login text)");
            statement.execute("INSERT INTO public.users VALUES ('00000000-0000-0000-0000-000000000002', 'legacy-user')");
        } catch (SQLException error) {
            throw new IllegalStateException("Could not prepare the legacy sentinel schema", error);
        }
    }

    /** Keeps integration-test compilation caches out of the developer's home directory. */
    @TestConfiguration
    public static class IsolatedCache {
        @Bean
        @Primary
        public CompilationCacheService isolatedCompilationCache() {
            try {
                return new CompilationCacheService(Files.createTempDirectory("toponavi-it-cache"));
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
        }
    }
}
