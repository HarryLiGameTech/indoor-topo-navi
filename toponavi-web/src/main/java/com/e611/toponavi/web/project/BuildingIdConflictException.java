package com.e611.toponavi.web.project;

/** A create request collided with an existing building ID, exactly or by case only. */
public class BuildingIdConflictException extends RuntimeException {
    public BuildingIdConflictException(String buildingId) {
        super("Building ID is already in use: " + buildingId);
    }
}
