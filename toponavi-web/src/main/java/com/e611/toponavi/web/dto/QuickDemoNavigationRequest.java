package com.e611.toponavi.web.dto;

import java.util.Collections;
import java.util.Map;

/** Request body for quick-demo navigation. */
public class QuickDemoNavigationRequest {
    public Map<String, Object> userParams = Collections.emptyMap();
    public TraversalPreferenceRequest traversalPreference;
}
