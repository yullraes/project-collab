package com.example.projectcollab.project.application;

import com.example.projectcollab.project.domain.ProjectNotFoundException;
import com.example.projectcollab.project.domain.ProjectPermissionException;
import com.example.projectcollab.project.domain.ProjectRepository;
import com.example.projectcollab.project.domain.ProjectResource;
import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.application.dto.ProjectResponse;
import com.example.projectcollab.project.application.dto.UpdateProjectRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public final class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(final ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponse createProject(final CreateProjectRequest request, final String actorUserId) {
        String ownerUserId = actorUserId;
        String name = normalized(request.name());
        String description = normalized(request.description());

        ProjectResource created = ProjectResource.create(name, description, ownerUserId);
        projectRepository.save(created);
        return toResponse(created);
    }

    public ProjectResponse updateProject(final long projectId, final UpdateProjectRequest request, final String actorUserId) {
        ProjectResource current = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (!canModifyBy(current, actorUserId)) {
            throw new ProjectPermissionException(actorUserId, "project.update.forbidden");
        }

        String nextName = normalized(request.name());
        String nextDescription = normalized(request.description());
        String targetName = (nextName == null || nextName.isBlank()) ? current.name() : nextName;
        String targetDescription = (nextDescription == null) ? current.description() : nextDescription;

        current.changeBasicInfo(targetName, targetDescription);
        return toResponse(projectRepository.save(current));
    }

    public void deleteProject(final long projectId, final String actorUserId) {
        ProjectResource current = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (!current.isOwner(actorUserId)) {
            throw new ProjectPermissionException(actorUserId, "project.delete.forbidden");
        }

        projectRepository.deleteById(projectId);
    }

    public ProjectResponse getProject(final long projectId, final String actorUserId) {
        ProjectResource current = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (!canReadProject(current, actorUserId)) {
            throw new ProjectPermissionException(actorUserId, "project.read.forbidden");
        }

        return toResponse(current);
    }

    public List<ProjectResponse> getMyProjects(final String actorUserId) {
        return projectRepository.findMyProjects(actorUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean canModifyBy(final ProjectResource project, final String actorUserId) {
        return project.isOwner(actorUserId) || project.isAdmin(actorUserId);
    }

    private boolean canReadProject(final ProjectResource project, final String actorUserId) {
        return canModifyBy(project, actorUserId);
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
                project.adminUserIds(),
                project.createdAt(),
                project.updatedAt()
        );
    }
}
