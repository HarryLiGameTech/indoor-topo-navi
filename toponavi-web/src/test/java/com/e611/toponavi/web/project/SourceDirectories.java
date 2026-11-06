package com.e611.toponavi.web.project;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads a native TopoScript source directory the same way the explicit importer does: recursively, accepted source
 * types only, relative POSIX paths, strict UTF-8, and no newline conversion. Test fixtures only.
 */
public final class SourceDirectories {
    private SourceDirectories() {
    }

    public static Map<String, String> read(Path directory) {
        Map<String, String> files = new TreeMap<>();
        try (var paths = Files.walk(directory)) {
            for (Path file : paths.filter(Files::isRegularFile).sorted().toList()) {
                String path = directory.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
                String basename = file.getFileName().toString();
                if (basename.contains(".") && !(basename.endsWith(".tmap") || basename.endsWith(".ttr") || basename.endsWith(".tcfg")))
                    continue;
                files.put(path, StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(Files.readAllBytes(file))).toString());
            }
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        return files;
    }

    /** Loads each immediate subdirectory as a project whose building ID is the directory name. */
    public static InMemoryProjectStore storeOf(Path root) {
        InMemoryProjectStore store = new InMemoryProjectStore();
        try (var children = Files.list(root)) {
            for (Path child : children.filter(Files::isDirectory).sorted().toList())
                store.put(child.getFileName().toString(), read(child));
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        return store;
    }
}
