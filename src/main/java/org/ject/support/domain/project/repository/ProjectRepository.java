package org.ject.support.domain.project.repository;

import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectQueryRepository {

    Page<ProjectResponse> findProjectsBySemester(String semester, Pageable pageable);

    Optional<ProjectDetailResponse> findProjectDetails(Long projectId);
}
