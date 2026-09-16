package com.example.projectcollab.project.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    @Query("""
            select p
              from ProjectEntity p, ProjectMemberEntity pm
             where p.projectId = pm.projectId
               and pm.userId = :userId
             order by p.projectId desc
            """)
    List<ProjectEntity> findAllByMemberUserId(@Param("userId") long userId);

}
