package com.example.projectcollab.task.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String creatorUserId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String description;

    private String assigneeUserId;

    @Column(nullable = false)
    private String state;

    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected TaskEntity() {
    }

    TaskEntity(
            final Long projectId,
            final String creatorUserId,
            final String title,
            final String description,
            final String assigneeUserId,
            final String state,
            final String rejectionReason
    ) {
        this.projectId = projectId;
        this.creatorUserId = creatorUserId;
        this.title = title;
        this.description = description;
        this.assigneeUserId = assigneeUserId;
        this.state = state;
        this.rejectionReason = rejectionReason;
    }

    void overwrite(
            final String title,
            final String description,
            final String assigneeUserId,
            final String state,
            final String rejectionReason
    ) {
        this.title = title;
        this.description = description;
        this.assigneeUserId = assigneeUserId;
        this.state = state;
        this.rejectionReason = rejectionReason;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long taskId() {
        return taskId;
    }

    public Long projectId() {
        return projectId;
    }

    public String creatorUserId() {
        return creatorUserId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String assigneeUserId() {
        return assigneeUserId;
    }

    public String state() {
        return state;
    }

    public String rejectionReason() {
        return rejectionReason;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
