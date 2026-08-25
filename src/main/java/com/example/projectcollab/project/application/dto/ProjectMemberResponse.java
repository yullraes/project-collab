package com.example.projectcollab.project.application.dto;

import com.example.projectcollab.project.domain.ProjectRole;

public record ProjectMemberResponse(String userId, ProjectRole role) {
}
