package com.example.projectcollab.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        long userId,

        @Schema(description = "사용자 이름", example = "데모 소유자")
        String name,

        @Schema(description = "정규화된 사용자 이메일", example = "owner@project-collab.example")
        String email,

        @Schema(description = "생성 시각", example = "2026-08-26T00:00:00Z")
        Instant createdAt
) {
}
