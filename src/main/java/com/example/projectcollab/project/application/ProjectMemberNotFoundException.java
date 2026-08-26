package com.example.projectcollab.project.application;

public final class ProjectMemberNotFoundException extends IllegalStateException {
    public ProjectMemberNotFoundException() {
        super("project.member.not_found");
    }
}
