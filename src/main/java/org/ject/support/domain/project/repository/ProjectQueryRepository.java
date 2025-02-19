package org.ject.support.domain.project.repository;

import java.util.Optional;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectQueryRepository {
    Page<ProjectResponse> findProjectsBySemester(String semester, Pageable pageable);

    Optional<ProjectDetailResponse> findProjectDetails(Long projectId);
}
