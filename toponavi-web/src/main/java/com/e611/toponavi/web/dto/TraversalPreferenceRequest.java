package com.e611.toponavi.web.dto;

import java.util.Collections;
import java.util.List;

/** Navigation-time preferences that must not affect the compilation cache key. */
public class TraversalPreferenceRequest {
    public String routePlanningPreference;
    public List<String> banTags = Collections.emptyList();
    public String minimizeTag;
    public String maximizeTag;
    public String riskPreference;
}
