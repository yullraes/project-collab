package com.example.projectcollab.project.persistence;

import com.example.projectcollab.project.domain.ProjectRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "project_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_members_project_user",
                columnNames = {"project_id", "user_id"}
        ),
        indexes = @Index(name = "idx_project_members_user_id", columnList = "user_id")
)
public class ProjectMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectMemberId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ProjectMemberEntity() {
    }

    private ProjectMemberEntity(final Long projectId, final Long userId, final ProjectRole role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
    }

    public static ProjectMemberEntity create(
            final long projectId,
            final long userId,
            final ProjectRole role
    ) {
        return new ProjectMemberEntity(projectId, userId, role);
    }

    public void changeRole(final ProjectRole role) {
        this.role = role;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long projectMemberId() {
        return projectMemberId;
    }

    public Long projectId() {
        return projectId;
    }

    public Long userId() {
        return userId;
    }

    public ProjectRole role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
