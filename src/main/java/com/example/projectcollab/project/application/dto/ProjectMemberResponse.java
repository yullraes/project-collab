package com.example.projectcollab.project.application.dto;

import com.example.projectcollab.project.domain.ProjectRole;

import java.time.Instant;

public record ProjectMemberResponse(long userId, ProjectRole role, Instant joinedAt) {
}
