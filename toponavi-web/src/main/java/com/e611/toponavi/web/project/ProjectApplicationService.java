package com.e611.toponavi.web.project;

import com.e611.toponavi.web.api.ApiException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Project CRUD orchestration: storage validation, store calls, and boundary error mapping. Saving never compiles
 * sources, calls an external service, or exports files; compilation validity is independent of storage validity.
 */
@Service
public class ProjectApplicationService {
    public static final int MAX_PAGE_SIZE = 200;

    private final ProjectStore store;
    private final ProjectStorageValidator validator;

    public ProjectApplicationService(ProjectStore store, ProjectStorageValidator validator) {
        this.store = store;
        this.validator = validator;
    }

    public ProjectSummary create(String buildingId, String name, Map<String, String> files) {
        validator.validateCreate(buildingId, name, files);
        return storage(() -> {
            try {
                return store.create(buildingId, name, files);
            } catch (BuildingIdConflictException conflict) {
                throw new ApiException(409, "BUILDING_ID_CONFLICT",
                        "The building ID is already used by a stored project, exactly or differing only by case.",
                        Map.of("path", "/buildingId"));
            }
        });
    }

    public ProjectPage list(int page, int size) {
        if (page < 0) throw ProjectStorageValidator.invalid("/page", "page must be a nonnegative integer.");
        if (size < 1 || size > MAX_PAGE_SIZE) throw ProjectStorageValidator.invalid("/size", "size must be an integer from 1 to 200.");
        return storage(() -> store.list(page, size));
    }

    public ProjectSnapshot get(UUID id) {
        return found(storage(() -> store.findById(id)));
    }

    public ProjectSnapshot getByBuildingId(String buildingId) {
        return found(storage(() -> store.findByBuildingId(buildingId)));
    }

    public ProjectSummary replace(UUID id, String name, Map<String, String> files) {
        validator.validateReplace(name, files);
        return found(storage(() -> store.replace(id, name, files)));
    }

    public void delete(UUID id) {
        if (!storage(() -> store.delete(id))) throw notFound();
    }

    private static <T> T found(Optional<T> value) {
        return value.orElseThrow(ProjectApplicationService::notFound);
    }

    private static ApiException notFound() {
        return new ApiException(404, "PROJECT_NOT_FOUND", "No stored project has this identifier.");
    }

    private static <T> T storage(Supplier<T> work) {
        try {
            return work.get();
        } catch (ProjectStoreUnavailableException unavailable) {
            throw new ApiException(503, "PROJECT_STORE_UNAVAILABLE", "Project storage is temporarily unavailable.");
        }
    }
}
