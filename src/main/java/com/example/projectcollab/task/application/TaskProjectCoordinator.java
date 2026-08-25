package com.example.projectcollab.task.application;

import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TaskProjectCoordinator {
    private final TaskRepository taskRepository;

    public TaskProjectCoordinator(final TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void normalizeAssignments(final long projectId, final String removedUserId) {
        List<Task> tasks = taskRepository.findAssignedTasksForMembershipEnd(projectId, removedUserId);
        tasks.forEach(Task::removeAssigneeForMembershipEnd);
        taskRepository.saveAll(tasks);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteProjectTasks(final long projectId) {
        taskRepository.deleteProjectTasks(projectId);
    }
}
