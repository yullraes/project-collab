package com.example.projectcollab.task.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.task.application.dto.TaskPageResponse;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskProjectSnapshot;
import com.example.projectcollab.task.domain.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskReadService {
    private static final int MAX_PAGE_SIZE = 100;

    private final TaskQueryRepository taskQueryRepository;
    private final TaskRepository taskRepository;

    public TaskReadService(
            final TaskQueryRepository taskQueryRepository,
            final TaskRepository taskRepository
    ) {
        this.taskQueryRepository = taskQueryRepository;
        this.taskRepository = taskRepository;
    }

    public TaskPageResponse listTasks(
            final long projectId,
            final String userId,
            final String keyword,
            final Task.TaskState state,
            final int page,
            final int size
    ) {
        requireProjectMember(projectId, userId);
        if (page < 0) {
            throw new IllegalArgumentException("task.page.invalid");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("task.page_size.invalid");
        }

        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
            normalizedKeyword = null;
        }
        return TaskPageResponse.from(taskQueryRepository.searchReadable(
                projectId,
                userId,
                normalizedKeyword,
                state,
                page,
                size
        ));
    }

    public TaskResponse getTaskDetail(final long projectId, final long taskId, final String userId) {
        requireProjectMember(projectId, userId);
        return taskQueryRepository.findReadableTask(projectId, taskId, userId)
                .orElseThrow(TaskNotFoundException::new);
    }

    private void requireProjectMember(final long projectId, final String userId) {
        TaskProjectSnapshot project = taskRepository.findProjectSnapshot(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        if (!project.isMember(userId)) {
            throw new ProjectPermissionException("task.read.forbidden");
        }
    }
}
