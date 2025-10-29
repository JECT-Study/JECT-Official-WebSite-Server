package org.ject.support.domain.apply.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplyQueryRepository {
    Page<Apply> findApplies(final JobFamily jobFamily,
                            final Apply.Status status,
                            final Pageable pageable);
}
