package com.example.projectcollab.project.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.project.domain.ProjectRepository;
import com.example.projectcollab.project.domain.ProjectResource;
import com.example.projectcollab.project.domain.ProjectRole;
import com.example.projectcollab.project.application.dto.AddProjectMemberRequest;
import com.example.projectcollab.project.application.dto.ChangeProjectRoleRequest;
import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.application.dto.ProjectMemberResponse;
import com.example.projectcollab.project.application.dto.ProjectResponse;
import com.example.projectcollab.project.application.dto.UpdateProjectRequest;
import com.example.projectcollab.task.application.TaskProjectCoordinator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskProjectCoordinator taskProjectCoordinator;

    public ProjectService(
            final ProjectRepository projectRepository,
            final TaskProjectCoordinator taskProjectCoordinator
    ) {
        this.projectRepository = projectRepository;
        this.taskProjectCoordinator = taskProjectCoordinator;
    }

    @Transactional
    public ProjectResponse createProject(final CreateProjectRequest request, final String actorUserId) {
        String ownerUserId = actorUserId;
        String name = normalized(request.name());
        String description = normalized(request.description());

        ProjectResource created = ProjectResource.create(name, description, ownerUserId);
        projectRepository.save(created);
        return toResponse(created);
    }

    @Transactional
    public ProjectResponse updateProject(final long projectId, final UpdateProjectRequest request, final String actorUserId) {
        ProjectResource current = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        if (!current.isManager(actorUserId)) {
            throw new ProjectPermissionException("project.update.forbidden");
        }

        String nextName = normalized(request.name());
        String nextDescription = normalized(request.description());
        String targetName = (nextName == null || nextName.isBlank()) ? current.name() : nextName;
        String targetDescription = (nextDescription == null) ? current.description() : nextDescription;

        current.changeBasicInfo(targetName, targetDescription);
        return toResponse(projectRepository.save(current));
    }

    @Transactional
    public void deleteProject(final long projectId, final String actorUserId) {
        ProjectResource current = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        if (!current.isOwner(actorUserId)) {
            throw new ProjectPermissionException("project.delete.forbidden");
        }

        taskProjectCoordinator.deleteProjectTasks(projectId);
        projectRepository.delete(current);
        projectRepository.flush();
    }

    public ProjectResponse getProject(final long projectId, final String actorUserId) {
        ProjectResource current = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        if (!current.isMember(actorUserId)) {
            throw new ProjectPermissionException("project.read.forbidden");
        }

        return toResponse(current);
    }

    public List<ProjectResponse> getMyProjects(final String actorUserId) {
        return projectRepository.findMyProjects(actorUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectMemberResponse addMember(
            final long projectId,
            final String actorUserId,
            final AddProjectMemberRequest request
    ) {
        ProjectResource project = loadForUpdate(projectId);
        requireManager(project, actorUserId, "project.member.add.forbidden");
        project.addMember(request.memberUserId());
        projectRepository.flush();
        return new ProjectMemberResponse(request.memberUserId(), ProjectRole.MEMBER);
    }

    public List<ProjectMemberResponse> getMembers(final long projectId, final String actorUserId) {
        ProjectResource project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        if (!project.isMember(actorUserId)) {
            throw new ProjectPermissionException("project.member.read.forbidden");
        }

        List<ProjectMemberResponse> members = new ArrayList<>();
        members.add(new ProjectMemberResponse(project.ownerUserId(), ProjectRole.OWNER));
        project.adminUserIds().forEach(userId -> members.add(new ProjectMemberResponse(userId, ProjectRole.ADMIN)));
        project.memberUserIds().forEach(userId -> members.add(new ProjectMemberResponse(userId, ProjectRole.MEMBER)));
        members.sort(Comparator.comparing(ProjectMemberResponse::role).thenComparing(ProjectMemberResponse::userId));
        return List.copyOf(members);
    }

    @Transactional
    public ProjectMemberResponse changeMemberRole(
            final long projectId,
            final String actorUserId,
            final String targetUserId,
            final ChangeProjectRoleRequest request
    ) {
        ProjectResource project = loadForUpdate(projectId);
        ProjectRole targetRole = project.roleOf(targetUserId);
        if (targetRole == null) {
            throw new IllegalStateException("project.member.not_found");
        }
        if (request.role() == null || request.role() == ProjectRole.OWNER) {
            throw new IllegalArgumentException("project.member.role.invalid");
        }

        if (request.role() == ProjectRole.ADMIN) {
            requireManager(project, actorUserId, "project.member.promote.forbidden");
            project.promoteToAdmin(targetUserId);
        } else {
            boolean selfDemotion = actorUserId.equals(targetUserId) && project.isAdmin(actorUserId);
            if (!project.isOwner(actorUserId) && !selfDemotion) {
                throw new ProjectPermissionException("project.member.demote.forbidden");
            }
            project.demoteToMember(targetUserId);
        }

        projectRepository.flush();
        return new ProjectMemberResponse(targetUserId, request.role());
    }

    @Transactional
    public void removeMember(
            final long projectId,
            final String actorUserId,
            final String targetUserId
    ) {
        ProjectResource project = loadForUpdate(projectId);
        ProjectRole actorRole = project.roleOf(actorUserId);
        ProjectRole targetRole = project.roleOf(targetUserId);
        if (actorRole == null || targetRole == null) {
            throw new ProjectPermissionException("project.member.remove.forbidden");
        }
        if (targetRole == ProjectRole.OWNER) {
            throw new ProjectPermissionException("project.owner.cannot_remove");
        }

        boolean selfLeave = actorUserId.equals(targetUserId);
        boolean ownerRemoval = actorRole == ProjectRole.OWNER;
        boolean adminRemovesMember = actorRole == ProjectRole.ADMIN && targetRole == ProjectRole.MEMBER;
        if (!selfLeave && !ownerRemoval && !adminRemovesMember) {
            throw new ProjectPermissionException("project.member.remove.forbidden");
        }

        taskProjectCoordinator.normalizeAssignments(projectId, targetUserId);
        project.removeMember(targetUserId);
        projectRepository.flush();
    }

    private ProjectResource loadForUpdate(final long projectId) {
        return projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private void requireManager(
            final ProjectResource project,
            final String actorUserId,
            final String code
    ) {
        if (!project.isManager(actorUserId)) {
            throw new ProjectPermissionException(code);
        }
    }

    private String normalized(final String value) {
        return value == null ? null : value.trim();
    }

    private ProjectResponse toResponse(final ProjectResource project) {
        return new ProjectResponse(
                project.projectId(),
                project.name(),
                project.description(),
                project.ownerUserId(),
                Set.copyOf(project.adminUserIds()),
                Set.copyOf(project.memberUserIds()),
                project.createdAt(),
                project.updatedAt()
        );
    }
}
