package com.e611.toponavi.web.dto;

/**
 * Request body for saving (updating) a sketch map.
 */
public class SaveMapRequest {
    public String mapJson;   // full map.json content
    public String label;     // optional commit label suffix
}

