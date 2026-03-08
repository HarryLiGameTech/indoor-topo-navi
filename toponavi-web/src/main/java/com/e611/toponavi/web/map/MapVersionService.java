package com.e611.toponavi.web.map;

import com.e611.toponavi.web.github.GitHubContentsService;
import com.e611.toponavi.web.model.Map;
import com.e611.toponavi.web.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Handles version-control operations (history, restore, named checkpoints).
 * All Git operations are performed by the bot via GitHubContentsService.
 */
@Service
public class MapVersionService {

    private final MapService mapService;
    private final MapAccessService accessService;
    private final GitHubContentsService contentsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MapVersionService(MapService mapService,
                             MapAccessService accessService,
                             GitHubContentsService contentsService) {
        this.mapService = mapService;
        this.accessService = accessService;
        this.contentsService = contentsService;
    }

    /**
     * Returns the commit history for the given map as a list of version summaries.
     * Each entry: { sha, message, author, date }
     */
    public List<java.util.Map<String, String>> listVersions(User user, UUID mapId) {
        Map map = mapService.requireMap(mapId);
        if (!accessService.canRead(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(map.getGithubRepo());
        String rawJson = contentsService.listCommits(repoName);
        return parseCommits(rawJson);
    }

    /**
     * Restores a map to the state at the given commit SHA by creating a new commit
     * with that commit's map.json content.
     */
    public void restoreVersion(User user, UUID mapId, String commitSha) {
        Map map = mapService.requireMap(mapId);
        if (!accessService.canWrite(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(map.getGithubRepo());
        String historicContent = contentsService.readFileAtCommit(repoName, "map.json", commitSha);
        if (historicContent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "map.json not found at commit " + commitSha);
        }
        String commitMsg = "[platform] " + user.getGithubLogin()
                + " restored to " + commitSha.substring(0, Math.min(7, commitSha.length()));
        contentsService.writeFile(repoName, "map.json", historicContent, commitMsg);
    }

    /**
     * Creates a named checkpoint (Git tag) at the latest commit of the map.
     */
    public void createCheckpoint(User user, UUID mapId, String label) {
        Map map = mapService.requireMap(mapId);
        if (!accessService.canWrite(user, map)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String repoName = extractRepoName(map.getGithubRepo());
        String latestSha = getLatestCommitSha(repoName);
        String tagName = "checkpoint-" + label.replaceAll("[^a-zA-Z0-9._-]", "-");
        contentsService.createTag(repoName, tagName, latestSha);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String extractRepoName(String fullRepoName) {
        int slash = fullRepoName.indexOf('/');
        return slash >= 0 ? fullRepoName.substring(slash + 1) : fullRepoName;
    }

    private String getLatestCommitSha(String repoName) {
        try {
            String rawJson = contentsService.listCommits(repoName);
            JsonNode commits = objectMapper.readTree(rawJson);
            if (commits.isArray() && commits.size() > 0) {
                return commits.get(0).get("sha").asText();
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No commits found");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to get latest commit: " + e.getMessage());
        }
    }

    private List<java.util.Map<String, String>> parseCommits(String rawJson) {
        List<java.util.Map<String, String>> result = new ArrayList<>();
        try {
            JsonNode commits = objectMapper.readTree(rawJson);
            for (JsonNode commit : commits) {
                java.util.Map<String, String> entry = new LinkedHashMap<>();
                entry.put("sha", commit.path("sha").asText());
                JsonNode inner = commit.path("commit");
                entry.put("message", inner.path("message").asText());
                entry.put("author", inner.path("author").path("name").asText());
                entry.put("date", inner.path("author").path("date").asText());
                result.add(entry);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to parse commit history: " + e.getMessage());
        }
        return result;
    }
}

