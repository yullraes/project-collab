package com.example.projectcollab.project.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, Long> {
    Optional<ProjectMemberEntity> findByProjectIdAndUserId(long projectId, long userId);

    boolean existsByProjectIdAndUserId(long projectId, long userId);

    List<ProjectMemberEntity> findAllByProjectIdOrderByProjectMemberId(long projectId);

    List<ProjectMemberEntity> findAllByUserId(long userId);

    void deleteAllByProjectId(long projectId);
}
