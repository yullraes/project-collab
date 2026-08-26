package com.example.projectcollab.task.domain;

public record Assignee(long userId) {
    public Assignee {
        Require.positive(userId, "담당자 사용자 ID는 양수여야 합니다.");
    }
}
