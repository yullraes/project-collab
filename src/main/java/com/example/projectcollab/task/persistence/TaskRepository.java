package com.example.projectcollab.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Optional<TaskEntity> findByTaskIdAndProjectId(final long taskId, final long projectId);

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.taskId = :taskId
              AND t.projectId = :projectId
              AND EXISTS (
                    SELECT p.projectId FROM ProjectResource p
                    WHERE p.projectId = t.projectId
                      AND (
                            p.ownerUserId = :requesterId
                            OR :requesterId MEMBER OF p.adminUserIds
                            OR :requesterId MEMBER OF p.memberUserIds
                          )
                  )
            """)
    Optional<TaskEntity> findReadableTask(
            @Param("projectId") long projectId,
            @Param("taskId") long taskId,
            @Param("requesterId") String requesterId
    );

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.projectId = :projectId
              AND EXISTS (
                    SELECT p.projectId FROM ProjectResource p
                    WHERE p.projectId = t.projectId
                      AND (
                            p.ownerUserId = :requesterId
                            OR :requesterId MEMBER OF p.adminUserIds
                            OR :requesterId MEMBER OF p.memberUserIds
                          )
                  )
              AND (:state IS NULL OR t.state = :state)
              AND (
                    :keyword IS NULL
                    OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<TaskEntity> searchReadable(
            @Param("projectId") long projectId,
            @Param("requesterId") String requesterId,
            @Param("keyword") String keyword,
            @Param("state") String state,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.projectId = :projectId AND t.assigneeUserId = :assigneeUserId
            ORDER BY t.taskId ASC
            """)
    List<TaskEntity> findAssignedTasksForUpdate(
            @Param("projectId") long projectId,
            @Param("assigneeUserId") String assigneeUserId
    );

    @Modifying
    @Query("DELETE FROM TaskEntity t WHERE t.projectId = :projectId")
    int deleteAllByProjectId(@Param("projectId") long projectId);
}
