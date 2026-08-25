package com.example.projectcollab.project.application.dto;

import java.time.Instant;
import java.util.Set;

public record ProjectResponse(
        long projectId,
        String name,
        String description,
        String ownerUserId,
        Set<String> adminUserIds,
        Set<String> memberUserIds,
        Instant createdAt,
        Instant updatedAt
) {
}
