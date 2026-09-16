package com.example.projectcollab.task.persistence;

import com.example.projectcollab.task.application.TaskQueryRepository;
import com.example.projectcollab.task.application.dto.TaskResponse;
import com.example.projectcollab.task.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaTaskQueryRepository implements TaskQueryRepository {
    private final SpringDataTaskRepository springDataRepository;

    public JpaTaskQueryRepository(final SpringDataTaskRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<TaskResponse> findReadableTask(
            final long projectId,
            final long taskId,
            final long requesterId
    ) {
        return springDataRepository.findReadableTask(projectId, taskId, requesterId)
                .map(this::toResponse);
    }

    @Override
    public Page<TaskResponse> searchReadable(
            final long projectId,
            final long requesterId,
            final String keyword,
            final Task.TaskState state,
            final int page,
            final int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("taskId"))
        );
        return springDataRepository.searchReadable(
                        projectId,
                        requesterId,
                        keyword,
                        state,
                        pageRequest
                )
                .map(this::toResponse);
    }

    private TaskResponse toResponse(final Task entity) {
        return new TaskResponse(
                entity.taskId(),
                entity.projectId(),
                entity.revision(),
                entity.creatorUserId(),
                entity.assigneeUserId(),
                entity.title(),
                entity.description(),
                entity.state().name(),
                entity.rejectionReason(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

}
