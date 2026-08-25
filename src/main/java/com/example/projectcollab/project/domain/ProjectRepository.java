package com.example.projectcollab.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectResource, Long> {
    @Query("""
            SELECT p FROM ProjectResource p
            WHERE p.ownerUserId = :actorUserId
               OR :actorUserId MEMBER OF p.adminUserIds
               OR :actorUserId MEMBER OF p.memberUserIds
            """)
    List<ProjectResource> findMyProjects(@Param("actorUserId") String actorUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProjectResource p WHERE p.projectId = :projectId")
    Optional<ProjectResource> findByIdForUpdate(@Param("projectId") long projectId);
}
