package org.ject.support.domain.jectalk.repository;

import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JectalkQueryRepository {
    Page<JectalkResponse> findJectalks(Pageable pageable, Project.Category category);
}
