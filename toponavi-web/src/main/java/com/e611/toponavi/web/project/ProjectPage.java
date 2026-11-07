package com.e611.toponavi.web.project;

import java.util.List;

/** One page of project summaries read together with the total in a single consistent transaction. */
public record ProjectPage(List<ProjectSummary> projects, int page, int size, long total) {
    public ProjectPage {
        projects = List.copyOf(projects);
    }
}
