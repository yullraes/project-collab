package com.example.projectcollab.task.persistence;

import com.example.projectcollab.task.domain.Assignee;
import com.example.projectcollab.task.domain.Creator;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskAssignment;
import com.example.projectcollab.task.domain.TaskContent;

final class TaskMapper {
    private TaskMapper() {
    }

    static Task toDomain(final TaskEntity taskEntity) {
        TaskAssignment assignment = taskEntity.assigneeUserId() == null
                ? TaskAssignment.Unassigned.INSTANCE
                : new TaskAssignment.Assigned(new Assignee(String.valueOf(taskEntity.assigneeUserId())));

        Task.TaskState state = Task.TaskState.valueOf(taskEntity.state());

        return Task.restore(
                taskEntity.taskId(),
                taskEntity.revision(),
                taskEntity.projectId(),
                new Creator(String.valueOf(taskEntity.creatorUserId())),
                new TaskContent(taskEntity.title(), taskEntity.description()),
                assignment,
                state,
                taskEntity.rejectionReason(),
                taskEntity.createdAt(),
                taskEntity.updatedAt()
        );
    }

    static TaskEntity toEntity(final Task task) {
        Long assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? Long.valueOf(assigned.assignee().username())
                : null;

        return new TaskEntity(
                task.projectId(),
                Long.valueOf(task.creator().username()),
                task.content().title(),
                task.content().description(),
                assigneeUserId,
                task.state().name(),
                task.rejectionReason()
        );
    }

    static void apply(final TaskEntity taskEntity, final Task task) {
        Long assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? Long.valueOf(assigned.assignee().username())
                : null;

        taskEntity.overwrite(
                task.content().title(),
                task.content().description(),
                assigneeUserId,
                task.state().name(),
                task.rejectionReason()
        );
    }
}
