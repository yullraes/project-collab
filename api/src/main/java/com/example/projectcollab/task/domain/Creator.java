package com.example.projectcollab.task.domain;

public record Creator(long userId) {
    public Creator {
        Require.positive(userId, "생성자 사용자 ID는 양수여야 합니다.");
    }
}
