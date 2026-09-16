package com.example.projectcollab.task.application.dto;

import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskAssignment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record TaskResponse(
        @Schema(description = "작업 ID", example = "1")
        Long taskId,

        @Schema(description = "소속 프로젝트 ID", example = "1")
        Long projectId,

        @Schema(description = "변경 요청에 다시 전달할 현재 리비전", example = "1")
        Long revision,

        @Schema(description = "작업 생성자 사용자 ID", example = "3")
        Long creatorUserId,

        @Schema(description = "현재 담당자 사용자 ID. 미할당이면 null입니다.", example = "3", nullable = true)
        Long assigneeUserId,

        @Schema(description = "작업 제목", example = "결제 오류 재현")
        String title,

        @Schema(description = "작업 내용과 완료 조건", example = "실패 조건을 재현하고 원인을 기록합니다.")
        String description,

        @Schema(description = "현재 작업 상태", example = "ACCEPTED")
        String state,

        @Schema(description = "최신 반려 사유. 반려 상태가 아니면 null입니다.", nullable = true)
        String rejectionReason,

        @Schema(description = "생성 시각", example = "2026-08-26T00:00:00Z")
        Instant createdAt,

        @Schema(description = "최근 수정 시각", example = "2026-08-26T00:01:00Z")
        Instant updatedAt
) {

    public static TaskResponse from(final Task task) {
        Long assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? assigned.assignee().userId()
                : null;
        return new TaskResponse(
                task.taskId(),
                task.projectId(),
                task.revision(),
                task.creator().userId(),
                assigneeUserId,
                task.content().title(),
                task.content().description(),
                task.state().name(),
                task.rejectionReason(),
                task.createdAt(),
                task.updatedAt()
        );
    }
}
