package com.e611.toponavi.web.dto;

import java.util.Map;

public class NavigationRequest {
    public Map<String, String> files; // "map1.tmap": "TopoMap...", "config.tcfg": "..."
    public String startNode;
    public String endNode;
}