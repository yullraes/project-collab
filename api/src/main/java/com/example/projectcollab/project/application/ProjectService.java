package com.example.projectcollab.project.application;

import com.example.projectcollab.project.application.dto.AddProjectMemberRequest;
import com.example.projectcollab.project.application.dto.ChangeProjectRoleRequest;
import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.application.dto.ProjectMemberResponse;
import com.example.projectcollab.project.application.dto.ProjectResponse;
import com.example.projectcollab.project.application.dto.UpdateProjectRequest;
import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.project.domain.ProjectRole;
import com.example.projectcollab.project.persistence.ProjectEntity;
import com.example.projectcollab.project.persistence.ProjectMemberEntity;
import com.example.projectcollab.project.persistence.ProjectMemberRepository;
import com.example.projectcollab.project.persistence.ProjectRepository;
import com.example.projectcollab.task.application.TaskProjectCoordinator;
import com.example.projectcollab.user.persistence.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskProjectCoordinator taskProjectCoordinator;

    public ProjectService(
            final ProjectRepository projectRepository,
            final ProjectMemberRepository projectMemberRepository,
            final UserRepository userRepository,
            final TaskProjectCoordinator taskProjectCoordinator
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskProjectCoordinator = taskProjectCoordinator;
    }

    @Transactional
    public ProjectResponse createProject(final CreateProjectRequest request, final long actorUserId) {
        requireUser(actorUserId);
        if (request == null) {
            throw new ProjectValidationException("project.input.required");
        }

        String name = ProjectInputNormalizer.name(request.name());
        String description = ProjectInputNormalizer.description(request.description());
        ProjectEntity project = projectRepository.saveAndFlush(ProjectEntity.create(name, description));
        projectMemberRepository.saveAndFlush(
                ProjectMemberEntity.create(project.projectId(), actorUserId, ProjectRole.OWNER)
        );
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(
            final long projectId,
            final UpdateProjectRequest request,
            final long actorUserId
    ) {
        ProjectEntity project = loadProject(projectId);
        requireManager(projectId, actorUserId);
        if (request == null) {
            throw new ProjectValidationException("project.input.required");
        }

        project.changeBasicInfo(
                ProjectInputNormalizer.name(request.name()),
                ProjectInputNormalizer.description(request.description())
        );
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse getProject(final long projectId, final long actorUserId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        requireMember(projectId, actorUserId);
        return toResponse(project);
    }

    public List<ProjectResponse> getMyProjects(final long actorUserId) {
        requireUser(actorUserId);
        return projectRepository.findAllByMemberUserId(actorUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteProject(final long projectId, final long actorUserId) {
        ProjectEntity project = loadProject(projectId);
        requireOwner(projectId, actorUserId);

        taskProjectCoordinator.deleteProjectTasks(projectId);
        projectMemberRepository.deleteAllByProjectId(projectId);
        projectMemberRepository.flush();
        projectRepository.delete(project);
        projectRepository.flush();
    }

    @Transactional
    public ProjectMemberResponse addMember(
            final long projectId,
            final long actorUserId,
            final AddProjectMemberRequest request
    ) {
        ProjectEntity project = loadProject(projectId);
        requireManager(projectId, actorUserId);
        if (request == null || request.userId() <= 0) {
            throw new ProjectValidationException("project.member.user_id.required");
        }
        requireUser(request.userId());
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new ProjectMemberAlreadyExistsException();
        }

        ProjectMemberEntity member;
        try {
            member = projectMemberRepository.saveAndFlush(
                    ProjectMemberEntity.create(projectId, request.userId(), ProjectRole.MEMBER)
            );
            project.markMembershipChanged();
            projectRepository.saveAndFlush(project);
        } catch (DataIntegrityViolationException exception) {
            throw new ProjectMemberAlreadyExistsException();
        }
        return toMemberResponse(member);
    }

    public List<ProjectMemberResponse> getMembers(final long projectId, final long actorUserId) {
        projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        requireMember(projectId, actorUserId);
        return projectMemberRepository.findAllByProjectIdOrderByProjectMemberId(projectId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse changeMemberRole(
            final long projectId,
            final long actorUserId,
            final long targetUserId,
            final ChangeProjectRoleRequest request
    ) {
        ProjectEntity project = loadProject(projectId);
        requireManager(projectId, actorUserId);
        ProjectMemberEntity target = requireTargetMember(projectId, targetUserId);
        if (request == null || request.role() == null) {
            throw new ProjectValidationException("project.member.role.invalid");
        }
        if (target.role() == request.role()) {
            throw new ProjectValidationException("project.member.role.invalid");
        }
        if (target.role() == ProjectRole.OWNER && request.role() != ProjectRole.OWNER) {
            requireAnotherOwner(projectId);
        }

        target.changeRole(request.role());
        project.markMembershipChanged();
        projectMemberRepository.flush();
        projectRepository.flush();
        return toMemberResponse(target);
    }

    @Transactional
    public void removeMember(final long projectId, final long actorUserId, final long targetUserId) {
        ProjectEntity project = loadProject(projectId);
        requireManager(projectId, actorUserId);
        ProjectMemberEntity target = requireTargetMember(projectId, targetUserId);
        if (target.role() == ProjectRole.OWNER) {
            requireAnotherOwner(projectId);
        }

        taskProjectCoordinator.normalizeAssignments(projectId, targetUserId);
        projectMemberRepository.delete(target);
        project.markMembershipChanged();
        projectMemberRepository.flush();
        projectRepository.flush();
    }

    private ProjectEntity loadProject(final long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private ProjectMemberEntity requireMember(final long projectId, final long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectPermissionException("project.access.forbidden"));
    }

    private ProjectMemberEntity requireTargetMember(final long projectId, final long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectMemberNotFoundException::new);
    }

    private ProjectMemberEntity requireManager(final long projectId, final long userId) {
        ProjectMemberEntity member = requireMember(projectId, userId);
        if (member.role() != ProjectRole.OWNER && member.role() != ProjectRole.ADMIN) {
            throw new ProjectPermissionException("project.access.forbidden");
        }
        return member;
    }

    private ProjectMemberEntity requireOwner(final long projectId, final long userId) {
        ProjectMemberEntity member = requireMember(projectId, userId);
        if (member.role() != ProjectRole.OWNER) {
            throw new ProjectPermissionException("project.owner.required");
        }
        return member;
    }

    private void requireAnotherOwner(final long projectId) {
        if (projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER) <= 1) {
            throw new ProjectPermissionException("project.owner.required");
        }
    }

    private void requireUser(final long userId) {
        if (userId <= 0 || !userRepository.existsById(userId)) {
            throw new ProjectUserNotFoundException();
        }
    }

    private ProjectResponse toResponse(final ProjectEntity project) {
        return new ProjectResponse(
                project.projectId(),
                project.name(),
                project.description(),
                project.createdAt(),
                project.updatedAt()
        );
    }

    private ProjectMemberResponse toMemberResponse(final ProjectMemberEntity member) {
        return new ProjectMemberResponse(member.userId(), member.role(), member.createdAt());
    }
}
