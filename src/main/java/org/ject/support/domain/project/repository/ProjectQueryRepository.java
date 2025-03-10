package org.ject.support.domain.project.repository;

import org.ject.support.common.data.RestPage;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.springframework.data.domain.Pageable;

public interface ProjectQueryRepository {

    RestPage<ProjectResponse> findProjectsByCategoryAndSemester(Project.Category category,
                                                                String semester,
                                                                Pageable pageable);
}
