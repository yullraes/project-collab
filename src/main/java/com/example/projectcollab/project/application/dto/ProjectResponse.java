package com.example.projectcollab.project.application.dto;

import java.time.Instant;

public record ProjectResponse(
        long projectId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
