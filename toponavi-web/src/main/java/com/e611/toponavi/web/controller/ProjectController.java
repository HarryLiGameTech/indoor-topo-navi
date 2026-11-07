package com.e611.toponavi.web.controller;

import com.e611.toponavi.web.project.ProjectApplicationService;
import com.e611.toponavi.web.project.ProjectPage;
import com.e611.toponavi.web.project.ProjectSnapshot;
import com.e611.toponavi.web.project.ProjectSummary;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.e611.toponavi.web.project.ProjectStorageValidator.escape;
import static com.e611.toponavi.web.project.ProjectStorageValidator.invalid;

/**
 * Service-authenticated CRUD for stored TopoScript projects, addressed by internal UUID. Lookup by exact
 * {@code buildingId} returns the same detail representation. Saving never compiles; see {@code /buildings} for that.
 */
@RestController
@RequestMapping(value = "/api/v1/projects", produces = "application/json")
public class ProjectController {
    private static final Pattern UUID_TEXT =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern NONNEGATIVE_INTEGER = Pattern.compile("[0-9]{1,10}");
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ProjectApplicationService projects;
    private final ProjectRequestReader requests;

    public ProjectController(ProjectApplicationService projects, ProjectRequestReader requests) {
        this.projects = projects;
        this.requests = requests;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Map<String, ?>> create(HttpServletRequest request) {
        rejectQuery(request);
        var body = requests.read(request, ProjectRequestReader.Kind.CREATE);
        ProjectSummary created = projects.create(body.buildingId(), body.name(), body.files());
        var location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(single(summary(created)));
    }

    @GetMapping
    public Map<String, ?> list(HttpServletRequest request) {
        Map<String, String[]> query = request.getParameterMap();
        for (var parameter : query.entrySet()) {
            String pointer = "/" + escape(parameter.getKey());
            if (!Set.of("page", "size").contains(parameter.getKey())) throw invalid(pointer, "Unknown query parameter.");
            if (parameter.getValue().length != 1) throw invalid(pointer, "Duplicate query parameter.");
        }
        int page = integer(query, "page", 0);
        int size = integer(query, "size", DEFAULT_PAGE_SIZE);
        ProjectPage result = projects.list(page, size);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("projects", result.projects().stream().map(ProjectController::summary).toList());
        response.put("page", result.page());
        response.put("size", result.size());
        response.put("total", result.total());
        return response;
    }

    @GetMapping("/{id}")
    public Map<String, ?> get(@PathVariable("id") String id, HttpServletRequest request) {
        rejectQuery(request);
        return single(detail(projects.get(uuid(id))));
    }

    @GetMapping("/by-building-id/{buildingId}")
    public Map<String, ?> getByBuildingId(@PathVariable("buildingId") String buildingId, HttpServletRequest request) {
        rejectQuery(request);
        return single(detail(projects.getByBuildingId(buildingId)));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public Map<String, ?> replace(@PathVariable("id") String id, HttpServletRequest request) {
        rejectQuery(request);
        UUID project = uuid(id);
        var body = requests.read(request, ProjectRequestReader.Kind.REPLACE);
        return single(summary(projects.replace(project, body.name(), body.files())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id, HttpServletRequest request) {
        rejectQuery(request);
        projects.delete(uuid(id));
        return ResponseEntity.noContent().build();
    }

    private static UUID uuid(String value) {
        if (!UUID_TEXT.matcher(value).matches()) throw invalid("/id", "Provide a project UUID.");
        return UUID.fromString(value);
    }

    private static int integer(Map<String, String[]> query, String key, int fallback) {
        if (!query.containsKey(key)) return fallback;
        String value = query.get(key)[0];
        if (!NONNEGATIVE_INTEGER.matcher(value).matches() || Long.parseLong(value) > Integer.MAX_VALUE)
            throw invalid("/" + key, key.equals("page") ? "page must be a nonnegative integer." : "size must be an integer from 1 to 200.");
        return Integer.parseInt(value);
    }

    private static void rejectQuery(HttpServletRequest request) {
        if (request.getQueryString() != null && !request.getQueryString().isEmpty())
            throw invalid("", "This operation does not accept query parameters.");
    }

    private static Map<String, ?> single(Map<String, Object> project) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("project", project);
        return response;
    }

    private static Map<String, Object> summary(ProjectSummary project) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", project.id().toString());
        result.put("buildingId", project.buildingId());
        result.put("name", project.name());
        result.put("createdAt", project.createdAt().toString());
        result.put("updatedAt", project.updatedAt().toString());
        return result;
    }

    private static Map<String, Object> detail(ProjectSnapshot project) {
        Map<String, Object> result = summary(project.summary());
        result.put("files", project.files());
        return result;
    }
}
