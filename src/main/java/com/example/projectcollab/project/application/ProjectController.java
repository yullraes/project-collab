package com.example.projectcollab.project.application;

import com.example.projectcollab.project.application.dto.CreateProjectRequest;
import com.example.projectcollab.project.application.dto.ProjectResponse;
import com.example.projectcollab.project.application.dto.UpdateProjectRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/projects")
public final class ProjectController {
    private final ProjectService projectService;

    public ProjectController(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectResponse> createProject(
            @RequestParam(name = "actorUserId") final String actorUserId,
            @RequestBody final CreateProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(request, actorUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(path = "/{projectId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable final long projectId,
            @RequestParam(name = "actorUserId") final String actorUserId,
            @RequestBody final UpdateProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(projectId, request, actorUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<java.util.List<ProjectResponse>> getMyProjects(
            @RequestParam(name = "actorUserId") final String actorUserId
    ) {
        return ResponseEntity.ok(projectService.getMyProjects(actorUserId));
    }

    @GetMapping(path = "/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable final long projectId,
            @RequestParam(name = "actorUserId") final String actorUserId
    ) {
        return ResponseEntity.ok(projectService.getProject(projectId, actorUserId));
    }

    @DeleteMapping(path = "/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable final long projectId,
            @RequestParam(name = "actorUserId") final String actorUserId
    ) {
        projectService.deleteProject(projectId, actorUserId);
        return ResponseEntity.noContent().build();
    }
}
