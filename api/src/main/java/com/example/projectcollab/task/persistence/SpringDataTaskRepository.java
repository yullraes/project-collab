package com.example.projectcollab.task.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.projectcollab.task.domain.Task;

import java.util.List;
import java.util.Optional;

public interface SpringDataTaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTaskIdAndProjectId(long taskId, long projectId);

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.taskId = :taskId
              AND t.projectId = :projectId
              AND EXISTS (
                    SELECT pm.projectMemberId FROM ProjectMemberEntity pm
                    WHERE pm.projectId = t.projectId
                      AND pm.userId = :requesterId
                  )
            """)
    Optional<Task> findReadableTask(
            @Param("projectId") long projectId,
            @Param("taskId") long taskId,
            @Param("requesterId") long requesterId
    );

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.projectId = :projectId
              AND EXISTS (
                    SELECT pm.projectMemberId FROM ProjectMemberEntity pm
                    WHERE pm.projectId = t.projectId
                      AND pm.userId = :requesterId
                  )
              AND (:state IS NULL OR t.state = :state)
              AND (
                    :keyword IS NULL
                    OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<Task> searchReadable(
            @Param("projectId") long projectId,
            @Param("requesterId") long requesterId,
            @Param("keyword") String keyword,
            @Param("state") Task.TaskState state,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE t.projectId = :projectId AND t.assigneeUserId = :assigneeUserId
            ORDER BY t.taskId ASC
            """)
    List<Task> findAssignedTasks(
            @Param("projectId") long projectId,
            @Param("assigneeUserId") long assigneeUserId
    );

    @Modifying
    @Query("DELETE FROM TaskEntity t WHERE t.projectId = :projectId")
    int deleteAllByProjectId(@Param("projectId") long projectId);
}
