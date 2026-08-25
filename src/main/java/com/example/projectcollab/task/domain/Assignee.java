package com.example.projectcollab.task.domain;

public record Assignee(String username) {
    public Assignee {
        username = Require.notBlank(username, "담당자 사용자 이름은 필수입니다.");
    }

    public static Assignee from(final Creator creator) {
        Require.notNull(creator, "생성자는 필수입니다.");
        return new Assignee(creator.username());
    }
}
