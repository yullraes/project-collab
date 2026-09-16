package com.example.projectcollab.task.application;

import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Task;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface TaskQueryRepository {
    Optional<TaskResponse> findReadableTask(long projectId, long taskId, long requesterId);

    Page<TaskResponse> searchReadable(
            long projectId,
            long requesterId,
            String keyword,
            Task.TaskState state,
            int page,
            int size
    );
}
