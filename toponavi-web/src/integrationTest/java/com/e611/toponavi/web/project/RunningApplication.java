package com.e611.toponavi.web.project;

import com.e611.toponavi.web.TopoNaviWebApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;

/** Starts the real application as a deployment would, so tests can stop and restart it against one database. */
final class RunningApplication implements AutoCloseable {
    private final ConfigurableApplicationContext context;

    private RunningApplication(ConfigurableApplicationContext context) {
        this.context = context;
    }

    static RunningApplication start(String jdbcUrl, String... extraProperties) {
        List<String> properties = new ArrayList<>(List.of(
                "server.port=0",
                "server.address=127.0.0.1",
                "spring.main.banner-mode=off",
                "spring.datasource.url=" + jdbcUrl,
                "spring.datasource.username=toponavi",
                "spring.datasource.password=toponavi",
                "platform.security.service-token=" + PostgresIntegrationSupport.TOKEN,
                "platform.api.authoring.allow-guests=false"));
        properties.addAll(List.of(extraProperties));
        // Command-line arguments outrank application.yml and environment variables, unlike default properties.
        return new RunningApplication(new SpringApplicationBuilder(TopoNaviWebApplication.class, PostgresIntegrationSupport.IsolatedCache.class)
                .run(properties.stream().map(property -> "--" + property).toArray(String[]::new)));
    }

    Http http(String token) {
        return new Http(((ServletWebServerApplicationContext) context).getWebServer().getPort(), token);
    }

    @Override
    public void close() {
        context.close();
    }
}
