package com.e611.toponavi.web.dto;

/**
 * Request body for creating a new sketch map.
 */
public class CreateMapRequest {
    public String title;
    public String initialMapJson;   // optional; defaults to "{}"
}

