package com.example.projectcollab.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectResource, Long> {
    @Query("SELECT p FROM ProjectResource p WHERE p.ownerUserId = :actorUserId OR :actorUserId MEMBER OF p.adminUserIds")
    List<ProjectResource> findMyProjects(@Param("actorUserId") String actorUserId);

    Optional<ProjectResource> findById(final long projectId);
}
