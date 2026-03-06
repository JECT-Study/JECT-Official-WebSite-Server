package org.ject.support.admin.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminApplyQueryRepository {
    Page<Apply> findAppliesByStatus(final JobFamily jobFamily,
                                    final Apply.Status status,
                                    final Long semesterId,
                                    final Pageable pageable);
}
