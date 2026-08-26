package com.example.projectcollab.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @Schema(description = "표시할 사용자 이름", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "중복될 수 없는 사용자 이메일", example = "hong@example.com")
        @NotBlank
        @Email
        String email
) {
}
