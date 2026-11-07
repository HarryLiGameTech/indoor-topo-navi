package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.api.ApiException;
import com.e611.toponavi.web.project.ProjectLimits;
import com.e611.toponavi.web.project.ProjectStorageValidator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.e611.toponavi.web.project.ProjectStorageValidator.escape;
import static com.e611.toponavi.web.project.ProjectStorageValidator.invalid;

/**
 * Reads project mutation bodies with endpoint-local rules: bounded decoded size checked while streaming (before the
 * body is buffered), no content encodings, strict envelope fields, and rejection of duplicate JSON object keys. The
 * application-wide JSON parser used by the computation endpoints is intentionally left unchanged.
 */
@Component
class ProjectRequestReader {
    enum Kind { CREATE, REPLACE }

    record ProjectRequest(String buildingId, String name, Map<String, String> files) {}

    private final ProjectLimits limits;
    private final ProjectStorageValidator validator;
    private final JsonFactory json;

    ProjectRequestReader(ProjectLimits limits, ProjectStorageValidator validator) {
        this.limits = limits;
        this.validator = validator;
        // The decoded body bound, not Jackson's default string cap, limits individual source strings.
        this.json = JsonFactory.builder().streamReadConstraints(StreamReadConstraints.builder()
                .maxStringLength((int) limits.maxRequestBytes()).build()).build();
    }

    ProjectRequest read(HttpServletRequest request, Kind kind) {
        String encoding = request.getHeader("Content-Encoding");
        if (encoding != null && !encoding.isBlank() && !encoding.trim().equalsIgnoreCase("identity"))
            throw invalid("", "Content encodings are not supported for project requests; send uncompressed JSON.");
        if (request.getContentLengthLong() > limits.maxRequestBytes()) throw requestTooLarge();
        try (JsonParser parser = json.createParser(new BoundedInputStream(request.getInputStream(), limits.maxRequestBytes()))) {
            return parse(parser, kind);
        } catch (BodyTooLargeException | StreamConstraintsException tooLarge) {
            throw requestTooLarge();
        } catch (JsonProcessingException malformed) {
            String pointer = malformed.getProcessor() instanceof JsonParser parser
                    ? parser.getParsingContext().pathAsPointer().toString() : "";
            throw invalid(pointer, "Provide a valid UTF-8 JSON request body.");
        } catch (IOException unreadable) {
            throw invalid("", "The request body could not be read.");
        }
    }

    private ProjectRequest parse(JsonParser parser, Kind kind) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) throw invalid("", "Expected a JSON object.");
        Set<String> seen = new HashSet<>();
        String buildingId = null, name = null;
        Map<String, String> files = null;
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            String field = parser.currentName();
            String pointer = "/" + escape(field);
            if (!seen.add(field)) throw invalid(pointer, "Duplicate field.");
            JsonToken value = parser.nextToken();
            switch (field) {
                case "buildingId" -> {
                    if (kind == Kind.REPLACE) throw invalid(pointer,
                            "A project's building ID cannot be changed; omit buildingId from replacement requests.");
                    buildingId = string(parser, value, pointer);
                }
                case "name" -> name = string(parser, value, pointer);
                case "files" -> files = files(parser, value);
                default -> throw invalid(pointer, "Unknown field.");
            }
        }
        if (parser.nextToken() != null) throw invalid("", "Unexpected content after the JSON object.");
        if (kind == Kind.CREATE && buildingId == null) throw invalid("/buildingId", "Required field is missing.");
        if (name == null) throw invalid("/name", "Required field is missing.");
        if (files == null) throw invalid("/files", "Required field is missing.");
        return new ProjectRequest(buildingId, name, files);
    }

    private static String string(JsonParser parser, JsonToken value, String pointer) throws IOException {
        if (value != JsonToken.VALUE_STRING) throw invalid(pointer, "Expected a string.");
        return parser.getText();
    }

    private Map<String, String> files(JsonParser parser, JsonToken value) throws IOException {
        if (value != JsonToken.START_OBJECT) throw invalid("/files", "Expected an object of source strings keyed by file path.");
        Map<String, String> files = new LinkedHashMap<>();
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            String path = parser.currentName();
            String pointer = "/files/" + escape(path);
            if (files.containsKey(path)) throw invalid(pointer, "Duplicate file path.");
            validator.checkFileCount(files.size() + 1);
            if (parser.nextToken() != JsonToken.VALUE_STRING) throw invalid(pointer, "Source contents must be strings.");
            files.put(path, parser.getText());
        }
        return files;
    }

    private ApiException requestTooLarge() {
        return ProjectStorageValidator.tooLarge("platform.projects.max-request-bytes", limits.maxRequestBytes(),
                "The project request body exceeds the configured size limit.");
    }

    private static final class BodyTooLargeException extends IOException {
    }

    /** Fails as soon as more than {@code limit} bytes are read, independent of any declared Content-Length. */
    private static final class BoundedInputStream extends FilterInputStream {
        private final long limit;
        private long count;

        BoundedInputStream(InputStream in, long limit) {
            super(in);
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value >= 0) count(1);
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int read = super.read(buffer, offset, length);
            if (read > 0) count(read);
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = super.skip(n);
            if (skipped > 0) count(skipped);
            return skipped;
        }

        private void count(long read) throws BodyTooLargeException {
            count += read;
            if (count > limit) throw new BodyTooLargeException();
        }
    }
}
