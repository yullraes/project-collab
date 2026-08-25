package com.example.projectcollab.project.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class ProjectResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String ownerUserId;

    @ElementCollection
    @CollectionTable(name = "project_admin_users", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "admin_user_id", nullable = false)
    private Set<String> adminUserIds = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ProjectResource() {
    }

    private ProjectResource(final String name, final String description, final String ownerUserId) {
        this.name = name;
        this.description = description;
        this.ownerUserId = ownerUserId;
    }

    public static ProjectResource create(final String name, final String description, final String ownerUserId) {
        return new ProjectResource(name, description, ownerUserId);
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

    public void changeBasicInfo(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public Long projectId() {
        return projectId;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String ownerUserId() {
        return ownerUserId;
    }

    public Set<String> adminUserIds() {
        return Collections.unmodifiableSet(adminUserIds);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public boolean isOwner(final String actorUserId) {
        return ownerUserId != null && ownerUserId.equals(actorUserId);
    }

    public boolean isAdmin(final String actorUserId) {
        return actorUserId != null && adminUserIds.contains(actorUserId);
    }
}
