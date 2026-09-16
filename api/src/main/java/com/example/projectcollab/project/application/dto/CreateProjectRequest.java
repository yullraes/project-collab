package com.example.projectcollab.project.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @Schema(description = "프로젝트 이름", example = "신규 협업 프로젝트")
        @NotBlank
        String name,

        @Schema(description = "프로젝트 목적과 범위", example = "팀의 신규 기능 개발 작업을 관리합니다.")
        @NotBlank
        String description
) {
}
