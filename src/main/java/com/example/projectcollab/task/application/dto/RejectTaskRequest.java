package com.example.projectcollab.task.application.dto;

public record RejectTaskRequest(
        String rejectionReason,

        Long revision
) {
}
