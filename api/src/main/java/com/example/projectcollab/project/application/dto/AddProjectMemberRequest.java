package com.example.projectcollab.project.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record AddProjectMemberRequest(
        @Schema(
                description = "추가할 기존 사용자 ID. 기본 실행의 MEMBER 예상 ID는 3이며 시작 로그가 최종 기준입니다.",
                example = "3"
        )
        @Positive
        long userId
) {
}
