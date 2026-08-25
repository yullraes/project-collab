package com.example.projectcollab.task.domain;

public record Assignee(String username) {
    public Assignee {
        username = Require.notBlank(username, "담당자 사용자 이름은 필수입니다.");
    }
}
