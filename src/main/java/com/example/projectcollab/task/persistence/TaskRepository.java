package com.example.projectcollab.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Optional<TaskEntity> findById(final long taskId);

    List<TaskEntity> findByProjectIdOrderByCreatedAtDesc(final Long projectId);
}
