package org.ject.support.domain.recruit.repository;

import java.util.Optional;

public interface SemesterQueryRepository {
    Optional<Long> findOngoingSemesterId();
}
