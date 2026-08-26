package com.example.projectcollab.project.application.dto;

import com.example.projectcollab.project.domain.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ProjectMemberResponse(
        @Schema(description = "프로젝트 멤버 사용자 ID", example = "3")
        long userId,

        @Schema(description = "프로젝트 역할", example = "MEMBER")
        ProjectRole role,

        @Schema(description = "프로젝트 참여 시각", example = "2026-08-26T00:00:00Z")
        Instant joinedAt
) {
}
