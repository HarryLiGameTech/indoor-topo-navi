package com.e611.toponavi.web.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Creates, deletes and updates GitHub repositories under the platform organisation
 * using the GitHub App Installation Token (bot-only actor).
 */
@Service
public class GitHubRepoService {

    private static final Logger log = LoggerFactory.getLogger(GitHubRepoService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String GITHUB_API = "https://api.github.com";

    @Value("${platform.github.org-name}")
    private String orgName;

    private final GitHubAppTokenService tokenService;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubRepoService(GitHubAppTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /** Returns the configured platform organisation name. */
    public String getOrgName() {
        return orgName;
    }

    /**
     * Creates a private repository under the platform org.
     *
     * @param repoName  short name (e.g. "map-{uuid}")
     * @return full repo name "org/repo"
     */
    public String createRepo(String repoName) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", repoName);
            body.put("private", true);
            body.put("auto_init", true);   // creates initial commit so the repo is non-empty

            Request req = authRequest()
                    .url(GITHUB_API + "/orgs/" + orgName + "/repos")
                    .post(RequestBody.create(objectMapper.writeValueAsString(body), JSON))
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                assertSuccess(resp, "create repo " + repoName);
                log.info("Created GitHub repo: {}/{}", orgName, repoName);
                return orgName + "/" + repoName;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GitHub repo: " + repoName, e);
        }
    }

    /**
     * Deletes a repository from the platform org.
     */
    public void deleteRepo(String repoName) {
        try {
            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName)
                    .delete()
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.code() == 404) {
                    log.warn("Tried to delete non-existent repo: {}/{}", orgName, repoName);
                    return;
                }
                assertSuccess(resp, "delete repo " + repoName);
                log.info("Deleted GitHub repo: {}/{}", orgName, repoName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete GitHub repo: " + repoName, e);
        }
    }

    /**
     * Updates the visibility of a repository.
     *
     * @param repoName   short name (e.g. "map-{uuid}")
     * @param isPublic   true → public, false → private
     */
    public void setRepoVisibility(String repoName, boolean isPublic) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("private", !isPublic);

            Request req = authRequest()
                    .url(GITHUB_API + "/repos/" + orgName + "/" + repoName)
                    .patch(RequestBody.create(objectMapper.writeValueAsString(body), JSON))
                    .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                assertSuccess(resp, "update visibility of " + repoName);
                log.info("Set {}/{} visibility to {}", orgName, repoName, isPublic ? "public" : "private");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update visibility of repo: " + repoName, e);
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

