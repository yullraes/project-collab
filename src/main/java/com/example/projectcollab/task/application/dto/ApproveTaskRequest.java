package com.example.projectcollab.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ApproveTaskRequest(
        @Schema(
                description = "승인과 함께 지정할 담당자 ID. 생략하면 기존 담당자를 유지합니다.",
                example = "3",
                nullable = true
        )
        @Positive
        Long assigneeUserId,

        @Schema(
                description = "승인과 함께 담당자를 해제할지 여부. assigneeUserId와 동시에 지정할 수 없습니다.",
                example = "false"
        )
        Boolean unassigned,

        @Schema(description = "조회 응답에서 받은 현재 작업 리비전", example = "0")
        @NotNull
        @PositiveOrZero
        Long revision
) {
}
