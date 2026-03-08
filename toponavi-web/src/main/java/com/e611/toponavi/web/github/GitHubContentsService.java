package com.e611.toponavi.web.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Reads and writes files inside platform-owned GitHub repositories.
 * Uses the GitHub Contents API with the bot Installation Access Token.
 */
@Service
public class GitHubContentsService {

    private static final Logger log = LoggerFactory.getLogger(GitHubContentsService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String GITHUB_API = "https://api.github.com";

    @Value("${platform.github.org-name}")
    private String orgName;

    private final GitHubAppTokenService tokenService;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubContentsService(GitHubAppTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Reads a file from a repo.
     *
     * @param repoName short repo name (e.g. "map-{uuid}")
     * @param path     file path inside the repo (e.g. "map.json")
     * @return decoded file content as a String
     */
    public String readFile(String repoName, String path) {
        try {
            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/contents/" + path)
                    .get()
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.code() == 404) return null;
                assertSuccess(resp, "read " + path);
                JsonNode node = objectMapper.readTree(resp.body().string());
                String encoded = node.get("content").asText().replaceAll("\\s", "");
                return new String(Base64.getDecoder().decode(encoded));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file " + path + " from " + repoName, e);
        }
    }

    /**
     * Creates or updates a file in a repo.
     *
     * @param repoName      short repo name
     * @param path          file path inside the repo
     * @param content       file content (UTF-8 string)
     * @param commitMessage commit message
     */
    public void writeFile(String repoName, String path, String content, String commitMessage) {
        try {
            String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String currentSha = getFileSha(repoName, path);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("message", commitMessage);
            body.put("content", encodedContent);
            if (currentSha != null) {
                body.put("sha", currentSha); // required for update
            }

            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/contents/" + path)
                    .put(RequestBody.create(objectMapper.writeValueAsString(body), JSON))
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                assertSuccess(resp, "write " + path);
                log.info("Wrote {} to {}/{}", path, orgName, repoName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write file " + path + " to " + repoName, e);
        }
    }

    /**
     * Returns the SHA of an existing file, or null if it doesn't exist.
     */
    private String getFileSha(String repoName, String path) {
        try {
            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/contents/" + path)
                    .get()
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.code() == 404) return null;
                assertSuccess(resp, "get SHA of " + path);
                JsonNode node = objectMapper.readTree(resp.body().string());
                return node.get("sha").asText();
            }
        } catch (Exception e) {
            log.warn("Could not get SHA for {}/{}/{}: {}", orgName, repoName, path, e.getMessage());
            return null;
        }
    }

    /**
     * Lists commits on the main branch of a repo.
     *
     * @return raw JSON array string from GitHub
     */
    public String listCommits(String repoName) {
        try {
            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/commits?per_page=50")
                    .get()
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                assertSuccess(resp, "list commits");
                return resp.body().string();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list commits for " + repoName, e);
        }
    }

    /**
     * Reads map.json at a specific commit SHA.
     */
    public String readFileAtCommit(String repoName, String path, String commitSha) {
        try {
            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/contents/" + path + "?ref=" + commitSha)
                    .get()
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.code() == 404) return null;
                assertSuccess(resp, "read " + path + " at " + commitSha);
                JsonNode node = objectMapper.readTree(resp.body().string());
                String encoded = node.get("content").asText().replaceAll("\\s", "");
                return new String(Base64.getDecoder().decode(encoded));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + path + " at " + commitSha, e);
        }
    }

    /**
     * Creates a Git tag on a specific commit.
     */
    public void createTag(String repoName, String tagName, String commitSha) {
        try {
            // 1. Create the tag object
            ObjectNode tagBody = objectMapper.createObjectNode();
            tagBody.put("tag", tagName);
            tagBody.put("message", "Checkpoint: " + tagName);
            tagBody.put("object", commitSha);
            tagBody.put("type", "commit");

            Request createTagReq = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/git/tags")
                    .post(RequestBody.create(objectMapper.writeValueAsString(tagBody), JSON))
                    .build();

            String tagSha;
            try (Response resp = httpClient.newCall(createTagReq).execute()) {
                assertSuccess(resp, "create tag object " + tagName);
                tagSha = objectMapper.readTree(resp.body().string()).get("sha").asText();
            }

            // 2. Create the reference
            ObjectNode refBody = objectMapper.createObjectNode();
            refBody.put("ref", "refs/tags/" + tagName);
            refBody.put("sha", tagSha);

            Request createRefReq = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName + "/git/refs")
                    .post(RequestBody.create(objectMapper.writeValueAsString(refBody), JSON))
                    .build();

            try (Response resp = httpClient.newCall(createRefReq).execute()) {
                assertSuccess(resp, "create tag ref " + tagName);
                log.info("Created tag {} on {}/{} at {}", tagName, orgName, repoName, commitSha);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tag " + tagName + " on " + repoName, e);
        }
    }

    // -----------------------------------------------------------------------

    private Request.Builder authRequest() {
        return new Request.Builder()
                .header("Authorization", "Bearer " + tokenService.getInstallationToken())
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28");
    }

    private void assertSuccess(Response resp, String action) throws Exception {
        if (!resp.isSuccessful()) {
            String body = resp.body() != null ? resp.body().string() : "(no body)";
            throw new RuntimeException("GitHub API error during [" + action + "] [" + resp.code() + "]: " + body);
        }
    }
}

