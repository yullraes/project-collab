package com.example.projectcollab.task.domain;

public record Creator(String username) {
    public Creator {
        username = Require.notBlank(username, "생성자 사용자 이름은 필수입니다.");
    }
}
