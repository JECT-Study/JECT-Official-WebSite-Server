package org.ject.support.domain.ministudy.repository;

import org.ject.support.common.data.RestPage;
import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.springframework.data.domain.Pageable;

public interface MiniStudyQueryRepository {
    RestPage<MiniStudyResponse> findMiniStudies(Pageable pageable);
}
