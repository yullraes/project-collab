package com.example.projectcollab.task.domain;

import java.util.Objects;

public final class TaskContent {
    private final String title;
    private final String description;

    private TaskContent(final String title, final String description) {
        this.title = Require.notBlank(title, "작업 제목은 필수입니다.");
        this.description = Require.notBlank(description, "작업 설명은 필수입니다.");
    }

    public static TaskContent of(final String title, final String description) {
        return new TaskContent(title, description);
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    @Override
    public boolean equals(final Object other) {
        return this == other
                || other instanceof TaskContent content
                && title.equals(content.title)
                && description.equals(content.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}
