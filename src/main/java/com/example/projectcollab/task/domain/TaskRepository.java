package com.example.projectcollab.task.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskResource, Long> {
    Optional<TaskResource> findById(final long taskId);

    List<TaskResource> findByProjectIdOrderByCreatedAtDesc(final Long projectId);
}
