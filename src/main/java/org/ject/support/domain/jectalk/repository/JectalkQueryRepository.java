package org.ject.support.domain.jectalk.repository;

import org.ject.support.common.data.RestPage;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.springframework.data.domain.Pageable;

public interface JectalkQueryRepository {
    RestPage<JectalkResponse> findJectalks(Pageable pageable);
}
