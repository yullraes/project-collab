package com.example.projectcollab.task.persistence;

import com.example.projectcollab.project.domain.ProjectRole;
import com.example.projectcollab.project.persistence.ProjectEntity;
import com.example.projectcollab.project.persistence.ProjectMemberEntity;
import com.example.projectcollab.project.persistence.ProjectMemberRepository;
import com.example.projectcollab.project.persistence.ProjectRepository;
import com.example.projectcollab.task.domain.Task;
import com.example.projectcollab.task.domain.TaskProjectSnapshot;
import com.example.projectcollab.task.domain.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class JpaTaskRepository implements TaskRepository {
    private final SpringDataTaskRepository springDataRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public JpaTaskRepository(
            final SpringDataTaskRepository springDataRepository,
            final ProjectRepository projectRepository,
            final ProjectMemberRepository projectMemberRepository
    ) {
        this.springDataRepository = springDataRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public Optional<TaskProjectSnapshot> findProjectSnapshot(final long projectId) {
        return projectRepository.findById(projectId).map(this::toTaskProjectSnapshot);
    }

    @Override
    public Task save(final Task task) {
        return springDataRepository.saveAndFlush(task);
    }

    @Override
    public void saveAll(final List<Task> tasks) {
        springDataRepository.flush();
    }

    @Override
    public Optional<Task> findByTaskIdAndProjectId(final long taskId, final long projectId) {
        return springDataRepository.findByTaskIdAndProjectId(taskId, projectId);
    }

    @Override
    public List<Task> findAssignedTasksForMembershipEnd(final long projectId, final long assigneeUserId) {
        return springDataRepository.findAssignedTasks(projectId, assigneeUserId);
    }

    @Override
    public void delete(final Task task) {
        springDataRepository.delete(task);
        springDataRepository.flush();
    }

    @Override
    public void deleteProjectTasks(final long projectId) {
        springDataRepository.deleteAllByProjectId(projectId);
        springDataRepository.flush();
    }

    private TaskProjectSnapshot toTaskProjectSnapshot(final ProjectEntity project) {
        List<ProjectMemberEntity> members = projectMemberRepository
                .findAllByProjectIdOrderByProjectMemberId(project.projectId());
        Set<Long> ownerUserIds = members.stream()
                .filter(member -> member.role() == ProjectRole.OWNER)
                .map(ProjectMemberEntity::userId)
                .collect(Collectors.toSet());
        Set<Long> adminUserIds = members.stream()
                .filter(member -> member.role() == ProjectRole.ADMIN)
                .map(ProjectMemberEntity::userId)
                .collect(Collectors.toSet());
        Set<Long> memberUserIds = members.stream()
                .filter(member -> member.role() == ProjectRole.MEMBER)
                .map(ProjectMemberEntity::userId)
                .collect(Collectors.toSet());
        return new TaskProjectSnapshot(
                ownerUserIds,
                adminUserIds,
                memberUserIds
        );
    }
}
