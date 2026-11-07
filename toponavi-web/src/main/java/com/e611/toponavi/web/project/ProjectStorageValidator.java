package com.e611.toponavi.web.project;

import com.e611.toponavi.web.api.ApiException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Storage validity for project requests: identity, filenames, source text, and size. It deliberately says nothing
 * about compilation; empty, incomplete, and non-compiling source sets are valid storage requests.
 *
 * <p>Values are checked exactly as submitted. Nothing is trimmed, normalized, case-folded, decoded, or rewritten.
 */
@Component
public class ProjectStorageValidator {
    static final Pattern BUILDING_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,254}");
    private static final Pattern DRIVE_OR_URI_PREFIX = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*:");
    private static final int MAX_NAME_CODE_POINTS = 255;
    private static final int MAX_PATH_BYTES = 512;

    private final ProjectLimits limits;

    public ProjectStorageValidator(ProjectLimits limits) {
        this.limits = limits;
    }

    public void validateCreate(String buildingId, String name, Map<String, String> files) {
        buildingId(buildingId);
        name(name);
        files(files);
    }

    public void validateReplace(String name, Map<String, String> files) {
        name(name);
        files(files);
    }

    void buildingId(String buildingId) {
        if (buildingId == null) throw invalid("/buildingId", "Provide a building ID.");
        if (!BUILDING_ID.matcher(buildingId).matches()) throw invalid("/buildingId",
                "Use 1 to 255 ASCII letters, digits, '.', '_' or '-', starting with a letter or digit.");
    }

    void name(String name) {
        if (name == null) throw invalid("/name", "Provide a display name.");
        if (name.isBlank()) throw invalid("/name", "The display name must not be blank.");
        if (name.codePointCount(0, name.length()) > MAX_NAME_CODE_POINTS)
            throw invalid("/name", "The display name must be at most 255 characters.");
        String problem = textProblem(name);
        if (problem != null) throw invalid("/name", "The display name " + problem + ".");
    }

    void files(Map<String, String> files) {
        if (files == null) throw invalid("/files", "Provide an object of source files keyed by path.");
        checkFileCount(files.size());
        long bytes = 0;
        for (var file : files.entrySet()) {
            String pointer = "/files/" + escape(file.getKey());
            path(file.getKey(), pointer);
            String content = file.getValue();
            if (content == null) throw invalid(pointer, "Source contents must be strings.");
            String problem = textProblem(content);
            if (problem != null) throw invalid(pointer, "Source text " + problem + ".");
            bytes += utf8Length(content);
            if (bytes > limits.maxSourceBytes()) throw tooLarge("platform.projects.max-source-bytes", limits.maxSourceBytes(),
                    "The project's source text exceeds the configured size limit.");
        }
    }

    /** Lets streaming request parsing stop as soon as the configured file count is exceeded. */
    public void checkFileCount(int count) {
        if (count > limits.maxFiles()) throw tooLarge("platform.projects.max-files", limits.maxFiles(),
                "The project has more source files than the configured limit.");
    }

    private void path(String path, String pointer) {
        if (path == null) throw invalid(pointer, "Provide a file path.");
        long bytes = utf8Length(path);
        if (bytes < 1 || bytes > MAX_PATH_BYTES) throw invalid(pointer, "File paths must be 1 to 512 UTF-8 bytes.");
        if (textProblem(path) != null) throw invalid(pointer, "File paths must be valid Unicode text without NUL characters.");
        if (path.codePoints().anyMatch(Character::isISOControl)) throw invalid(pointer, "File paths must not contain control characters.");
        if (path.indexOf('\\') >= 0) throw invalid(pointer, "Use '/' as the file path separator.");
        if (path.startsWith("/")) throw invalid(pointer, "File paths must be relative.");
        if (path.endsWith("/")) throw invalid(pointer, "File paths must name a file, not a directory.");
        if (DRIVE_OR_URI_PREFIX.matcher(path).find()) throw invalid(pointer, "File paths must not start with a drive or URI prefix.");
        String[] segments = path.split("/", -1);
        for (String segment : segments) {
            if (segment.isEmpty()) throw invalid(pointer, "File paths must not contain empty segments.");
            if (segment.equals(".") || segment.equals("..")) throw invalid(pointer, "File paths must not contain '.' or '..' segments.");
        }
        String basename = segments[segments.length - 1];
        if (basename.indexOf('.') >= 0 && !(basename.endsWith(".tmap") || basename.endsWith(".ttr") || basename.endsWith(".tcfg")))
            throw invalid(pointer, "Use an extensionless TopoScript source or a .tmap, .ttr, or .tcfg file.");
    }

    /** Text that cannot round-trip through PostgreSQL UTF-8 {@code text}: NUL or unpaired UTF-16 surrogates. */
    private static String textProblem(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\0') return "must not contain NUL characters";
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(i + 1))) return "must be valid Unicode";
                i++;
            } else if (Character.isLowSurrogate(c)) {
                return "must be valid Unicode";
            }
        }
        return null;
    }

    /** UTF-8 encoded length of valid text; unpaired surrogates (rejected separately) count as three bytes. */
    static long utf8Length(String value) {
        long bytes = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 0x80) bytes += 1;
            else if (c < 0x800) bytes += 2;
            else if (Character.isHighSurrogate(c) && i + 1 < value.length() && Character.isLowSurrogate(value.charAt(i + 1))) {
                bytes += 4;
                i++;
            } else bytes += 3;
        }
        return bytes;
    }

    /** Escapes one JSON Pointer reference token (RFC 6901). */
    public static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    public static ApiException invalid(String pointer, String message) {
        return new ApiException(400, "INVALID_REQUEST", message, Map.of("path", pointer));
    }

    public static ApiException tooLarge(String limit, long maximum, String message) {
        return new ApiException(413, "PROJECT_TOO_LARGE", message, Map.of("limit", limit, "maximum", maximum));
    }
}
