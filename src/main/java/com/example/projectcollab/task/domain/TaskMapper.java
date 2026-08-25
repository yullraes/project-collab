package com.example.projectcollab.task.domain;

public final class TaskMapper {
    private TaskMapper() {
    }

    public static Task toDomain(final TaskResource taskResource) {
        TaskAssignment assignment = taskResource.assigneeUserId() == null
                ? TaskAssignment.unassigned()
                : TaskAssignment.assigned(new Assignee(taskResource.assigneeUserId()));

        Task.TaskState state = Task.TaskState.valueOf(taskResource.state());

        return new Task(
                taskResource.projectId(),
                new Creator(taskResource.creatorUserId()),
                TaskContent.of(taskResource.title(), taskResource.description()),
                assignment,
                state,
                taskResource.rejectionReason()
        );
    }

    public static TaskResource toResource(final Task task) {
        String assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? assigned.assignee().username()
                : null;

        return new TaskResource(
                task.projectId(),
                task.creator().username(),
                task.content().title(),
                task.content().description(),
                assigneeUserId,
                task.state().name(),
                task.rejectionReason()
        );
    }

    public static void apply(final TaskResource taskResource, final Task task) {
        String assigneeUserId = task.assignment() instanceof TaskAssignment.Assigned assigned
                ? assigned.assignee().username()
                : null;

        taskResource.overwrite(
                task.content().title(),
                task.content().description(),
                assigneeUserId,
                task.state().name(),
                task.rejectionReason()
        );
    }
}
