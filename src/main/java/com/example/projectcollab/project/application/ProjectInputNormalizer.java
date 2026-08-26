package com.example.projectcollab.project.application;

public final class ProjectInputNormalizer {
    private ProjectInputNormalizer() {
    }

    public static String name(final String value) {
        if (value == null || value.isBlank()) {
            throw new ProjectValidationException("project.name.required");
        }
        return value.trim();
    }

    public static String description(final String value) {
        if (value == null || value.isBlank()) {
            throw new ProjectValidationException("project.description.required");
        }
        return value.trim();
    }
}
