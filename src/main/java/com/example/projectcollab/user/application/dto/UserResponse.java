package com.example.projectcollab.user.application.dto;

import java.time.Instant;

public record UserResponse(
        long userId,
        String name,
        String email,
        Instant createdAt
) {
}
