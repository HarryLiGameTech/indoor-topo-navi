package com.e611.toponavi.web.cache;

import compiler.CompilationResult;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class CompilationCacheService {

    private final Path cacheDir;

    public CompilationCacheService() {
        String home = System.getProperty("user.home");
        this.cacheDir = Paths.get(home, ".toponavi-cache");
        createCacheDirectoryIfNotExists();
    }

    /**
     * Loads cached CompilationResult if available and valid (matches current file hash).
     *
     * @param files Map of filename -> file content to check against cache
     * @return Optional CompilationResult if found and valid, empty otherwise
     */
    public Optional<CachedResult> load(java.util.Map<String, String> files) {
        String currentHash = generateCacheHash(files);
        Path cacheFile = cacheDir.resolve(currentHash + ".ser");
        Path metaFile = cacheDir.resolve(currentHash + ".meta");

        if (!Files.exists(cacheFile)) {
            return Optional.empty();
        }

        try {
            // Verify meta file hasn't been corrupted before deserializing
            if (Files.exists(metaFile)) {
                String storedHash = Files.readString(metaFile).trim();
                if (!storedHash.equals(currentHash)) {
                    System.out.println("Cache INVALIDATED: hash mismatch, recompiling needed");
                    Files.deleteIfExists(cacheFile); // clean up stale entry
                    return Optional.empty();
                }
            }

            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cacheFile))) {
                CompilationResult result = (CompilationResult) ois.readObject();
                System.out.println("Cache HIT: " + currentHash.substring(0, 8) + "...");
                return Optional.of(new CachedResult(result, currentHash));
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Cache read failed for " + currentHash.substring(0, 8) + "...: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Saves CompilationResult to cache along with hash metadata.
     *
     * @param files Map of filename -> file content to compute hash from
     * @param result CompilationResult to cache
     */
    public void save(java.util.Map<String, String> files, CompilationResult result) {
        String cacheHash = generateCacheHash(files);
        Path cacheFile = cacheDir.resolve(cacheHash + ".ser");
        Path metaFile = cacheDir.resolve(cacheHash + ".meta");

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cacheFile))) {
            oos.writeObject(result);

            // Write meta file with hash for validation
            Files.writeString(metaFile, cacheHash);

            System.out.println("Cache SAVED: " + cacheHash.substring(0, 8) + "...");
        } catch (IOException e) {
            System.err.println("Cache write failed for " + cacheHash.substring(0, 8) + "...: " + e.getMessage());
            throw new RuntimeException("Failed to cache compilation result", e);
        }
    }

    /**
     * Invalidates a specific cache entry by hash.
     *
     * @param cacheHash Hash of cache to invalidate
     * @return true if deleted, false otherwise
     */
    public boolean invalidate(String cacheHash) {
        Path cacheFile = cacheDir.resolve(cacheHash + ".ser");
        Path metaFile = cacheDir.resolve(cacheHash + ".meta");

        try {
            boolean deleted = Files.deleteIfExists(cacheFile);
            Files.deleteIfExists(metaFile);

            if (deleted) {
                System.out.println("Cache INVALIDATED: " + cacheHash.substring(0, 8) + "...");
            }
            return deleted;
        } catch (IOException e) {
            System.err.println("Failed to invalidate cache: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears all cached compilation results.
     */
    public void clearAll() {
        try {
            Files.walk(cacheDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.err.println("Failed to delete " + file + ": " + e.getMessage());
                    }
                });
            System.out.println("Cache CLEARED: all entries removed");
        } catch (IOException e) {
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }

    /**
     * Generates SHA-256 hash from all file contents.
     * Hash is deterministic and changes if ANY file content changes.
     *
     * @param files Map of filename -> file content
     * @return Hex-encoded SHA-256 hash
     */
    public String generateCacheHash(java.util.Map<String, String> files) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Sort filenames for consistent hash regardless of iteration order
            java.util.List<String> sortedFilenames = files.keySet().stream()
                .sorted()
                .toList();

            for (String filename : sortedFilenames) {
                String content = files.get(filename);
                if (content != null) {
                    digest.update(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }

            byte[] hash = digest.digest();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Helper method to create cache directory if it doesn't exist.
     */
    private void createCacheDirectoryIfNotExists() {
        try {
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                System.out.println("Created cache directory: " + cacheDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create cache directory: " + e.getMessage());
        }
    }

    /**
     * Wrapper class to hold both the result and its hash.
     */
    public static class CachedResult {
        private final CompilationResult result;
        private final String cacheHash;

        public CachedResult(CompilationResult result, String cacheHash) {
            this.result = result;
            this.cacheHash = cacheHash;
        }

        public CompilationResult getResult() {
            return result;
        }

        public String getCacheHash() {
            return cacheHash;
        }
    }
}
