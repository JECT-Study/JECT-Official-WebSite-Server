package org.ject.support.domain.ministudy.repository;

import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MiniStudyQueryRepository {
    Page<MiniStudyResponse> findMiniStudies(Pageable pageable);
}
