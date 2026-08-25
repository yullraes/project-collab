package com.example.projectcollab.task.application;

import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.persistence.TaskEntity;
import com.example.projectcollab.task.persistence.TaskMapper;
import com.example.projectcollab.task.persistence.TaskRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TaskProjectCoordinator {
    private final TaskRepository taskRepository;

    public TaskProjectCoordinator(final TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void normalizeAssignments(final long projectId, final String removedUserId) {
        for (TaskEntity entity : taskRepository.findAssignedTasksForUpdate(projectId, removedUserId)) {
            Task task = TaskMapper.toDomain(entity);
            task.removeAssigneeForMembershipEnd();
            TaskMapper.apply(entity, task);
        }
        taskRepository.flush();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteProjectTasks(final long projectId) {
        taskRepository.deleteAllByProjectId(projectId);
        taskRepository.flush();
    }
}
