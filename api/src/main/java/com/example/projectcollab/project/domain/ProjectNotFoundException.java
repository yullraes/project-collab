package com.example.projectcollab.project.domain;

public final class ProjectNotFoundException extends IllegalStateException {
    public ProjectNotFoundException() {
        super("project.not.found");
    }
}
