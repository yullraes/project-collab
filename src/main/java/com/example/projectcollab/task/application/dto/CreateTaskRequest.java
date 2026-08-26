package com.example.projectcollab.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateTaskRequest(
        @Schema(description = "작업 제목", example = "결제 오류 재현")
        @NotBlank
        String title,

        @Schema(description = "작업의 내용과 완료 조건", example = "실패 조건을 재현하고 원인을 기록합니다.")
        @NotBlank
        String description,

        @Schema(
                description = "담당자 사용자 ID. OWNER·ADMIN만 선택할 수 있으며 현재 프로젝트 멤버여야 합니다.",
                example = "3",
                nullable = true
        )
        @Positive
        Long assigneeUserId,

        @Schema(
                description = "담당자 없이 등록할지 여부. assigneeUserId와 동시에 지정할 수 없습니다.",
                example = "false"
        )
        Boolean unassigned
) {
}
