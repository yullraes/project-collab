package com.example.projectcollab.task.domain;

public record TaskContent(String title, String description) {
    public TaskContent {
        title = Require.notBlank(title, "작업 제목은 필수입니다.");
        description = Require.notBlank(description, "작업 설명은 필수입니다.");
    }
}
