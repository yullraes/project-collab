package com.example.projectcollab.task.application;

public final class TaskConflictException extends RuntimeException {
    public static final String MESSAGE = "작업이 다른 요청에 의해 변경되었습니다.";

    public TaskConflictException() {
        super(MESSAGE);
    }
}
