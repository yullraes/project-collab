package com.example.projectcollab.project.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ProjectResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        long projectId,

        @Schema(description = "프로젝트 이름", example = "Swagger 데모 프로젝트")
        String name,

        @Schema(description = "프로젝트 설명", example = "프로젝트 권한과 작업 상태 전이를 확인합니다.")
        String description,

        @Schema(description = "생성 시각", example = "2026-08-26T00:00:00Z")
        Instant createdAt,

        @Schema(description = "최근 수정 시각", example = "2026-08-26T00:01:00Z")
        Instant updatedAt
) {
}
