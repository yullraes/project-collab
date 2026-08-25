package com.example.projectcollab.task.domain;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);

    void saveAll(List<Task> tasks);

    Optional<Task> findByTaskIdAndProjectId(long taskId, long projectId);

    List<Task> findAssignedTasksForMembershipEnd(long projectId, String assigneeUserId);

    void delete(Task task);

    void deleteProjectTasks(long projectId);
}
