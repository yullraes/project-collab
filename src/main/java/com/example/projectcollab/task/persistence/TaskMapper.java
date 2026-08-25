package com.example.projectcollab.task.persistence;

import com.example.projectcollab.task.domain.Assignee;
import com.example.projectcollab.task.domain.Creator;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskAssignment;
import com.example.projectcollab.task.domain.TaskContent;

public final class TaskMapper {
    private TaskMapper() {
    }

    public static Task toDomain(final TaskEntity taskEntity) {
        TaskAssignment assignment = taskEntity.assigneeUserId() == null
                ? TaskAssignment.Unassigned.INSTANCE
                : new TaskAssignment.Assigned(new Assignee(taskEntity.assigneeUserId()));

        Task.TaskState state = Task.TaskState.valueOf(taskEntity.state());

        return Task.restore(
                taskEntity.projectId(),
                new Creator(taskEntity.creatorUserId()),
                new TaskContent(taskEntity.title(), taskEntity.description()),
                assignment,
                state,
                taskEntity.rejectionReason()
        );
    }

    public static TaskEntity toEntity(final Task task) {
        String assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? assigned.assignee().username()
                : null;

        return new TaskEntity(
                task.projectId(),
                task.creator().username(),
                task.content().title(),
                task.content().description(),
                assigneeUserId,
                task.state().name(),
                task.rejectionReason()
        );
    }

    public static void apply(final TaskEntity taskEntity, final Task task) {
        String assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? assigned.assignee().username()
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
