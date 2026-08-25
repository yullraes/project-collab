package com.example.projectcollab.project.domain;

public final class ProjectNotFoundException extends IllegalStateException {
    private final long projectId;

    public ProjectNotFoundException(final long projectId) {
        super("project.not.found");
        this.projectId = projectId;
    }

    public long projectId() {
        return projectId;
    }
}
