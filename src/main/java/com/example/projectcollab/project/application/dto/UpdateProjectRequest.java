package com.example.projectcollab.project.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequest(
        @Schema(description = "변경할 프로젝트 이름", example = "개편된 협업 프로젝트")
        @NotBlank
        String name,

        @Schema(description = "변경할 프로젝트 설명", example = "개편된 프로젝트 범위와 목표입니다.")
        @NotBlank
        String description
) {
}
