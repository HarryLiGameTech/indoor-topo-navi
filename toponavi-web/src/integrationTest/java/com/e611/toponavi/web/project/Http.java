package com.e611.toponavi.web.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Minimal real-HTTP client, so tests exercise the actual servlet container and security filter chain. */
public final class Http {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String base;
    private final String token;

    public Http(int port, String token) {
        this.base = "http://127.0.0.1:" + port;
        this.token = token;
    }

    public record Response(int status, JsonNode body, HttpResponse<String> raw) {
        public String code() { return body.path("error").path("code").asText(); }
        public JsonNode project() { return body.path("project"); }
    }

    public Response get(String path) { return send("GET", path, null); }
    public Response delete(String path) { return send("DELETE", path, null); }
    public Response post(String path, Object body) { return send("POST", path, body); }
    public Response put(String path, Object body) { return send("PUT", path, body); }

    public Response send(String method, String path, Object body) {
        try {
            var builder = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(60));
            if (token != null) builder.header("Authorization", "Bearer " + token);
            if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
            else builder.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(
                    body instanceof String text ? text : MAPPER.writeValueAsString(body), StandardCharsets.UTF_8));
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String text = response.body();
            return new Response(response.statusCode(), text == null || text.isEmpty() ? MAPPER.nullNode() : MAPPER.readTree(text), response);
        } catch (IOException error) {
            throw new java.io.UncheckedIOException(error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error);
        }
    }
}
