package com.example.projectcollab.project.application.dto;

import com.example.projectcollab.project.domain.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeProjectRoleRequest(
        @Schema(description = "변경할 프로젝트 역할", example = "ADMIN")
        @NotNull
        ProjectRole role
) {
}
