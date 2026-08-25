package com.example.projectcollab.task.persistence;

import com.example.projectcollab.project.domain.ProjectRepository;
import com.example.projectcollab.project.domain.ProjectResource;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskProjectSnapshot;
import com.example.projectcollab.task.domain.TaskRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaTaskRepository implements TaskRepository {
    private final SpringDataTaskRepository springDataRepository;
    private final ProjectRepository projectRepository;

    public JpaTaskRepository(
            final SpringDataTaskRepository springDataRepository,
            final ProjectRepository projectRepository
    ) {
        this.springDataRepository = springDataRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Optional<TaskProjectSnapshot> findProjectSnapshot(final long projectId) {
        return projectRepository.findById(projectId).map(this::toTaskProjectSnapshot);
    }

    @Override
    public Optional<TaskProjectSnapshot> findProjectSnapshotForUpdate(final long projectId) {
        return projectRepository.findByIdForUpdate(projectId).map(this::toTaskProjectSnapshot);
    }

    @Override
    public Task save(final Task task) {
        TaskEntity entity;
        if (task.taskId() == null) {
            entity = TaskMapper.toEntity(task);
        } else {
            entity = loadCurrent(task);
            requireSameRevision(entity, task);
            TaskMapper.apply(entity, task);
        }
        return TaskMapper.toDomain(springDataRepository.saveAndFlush(entity));
    }

    @Override
    public void saveAll(final List<Task> tasks) {
        for (Task task : tasks) {
            TaskEntity entity = loadCurrent(task);
            requireSameRevision(entity, task);
            TaskMapper.apply(entity, task);
        }
        springDataRepository.flush();
    }

    @Override
    public Optional<Task> findByTaskIdAndProjectId(final long taskId, final long projectId) {
        return springDataRepository.findByTaskIdAndProjectId(taskId, projectId)
                .map(TaskMapper::toDomain);
    }

    @Override
    public List<Task> findAssignedTasksForMembershipEnd(final long projectId, final String assigneeUserId) {
        return springDataRepository.findAssignedTasksForUpdate(projectId, assigneeUserId).stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(final Task task) {
        TaskEntity entity = loadCurrent(task);
        requireSameRevision(entity, task);
        springDataRepository.delete(entity);
        springDataRepository.flush();
    }

    @Override
    public void deleteProjectTasks(final long projectId) {
        springDataRepository.deleteAllByProjectId(projectId);
        springDataRepository.flush();
    }

    private TaskEntity loadCurrent(final Task task) {
        Long taskId = task.taskId();
        if (taskId == null) {
            throw new IllegalArgumentException("저장된 작업의 ID는 필수입니다.");
        }
        return springDataRepository.findByTaskIdAndProjectId(taskId, task.projectId())
                .orElseThrow(() -> new ObjectOptimisticLockingFailureException(TaskEntity.class, taskId));
    }

    private void requireSameRevision(final TaskEntity entity, final Task task) {
        if (!Objects.equals(entity.revision(), task.revision())) {
            throw new ObjectOptimisticLockingFailureException(TaskEntity.class, task.taskId());
        }
    }

    private TaskProjectSnapshot toTaskProjectSnapshot(final ProjectResource project) {
        return new TaskProjectSnapshot(
                project.ownerUserId(),
                project.adminUserIds(),
                project.memberUserIds()
        );
    }
}
