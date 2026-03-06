package org.ject.support.admin.apply.repository;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminApplyQueryRepository {
    Page<Apply> findAppliesByStatus(final JobFamily jobFamily,
                                    final Apply.Status status,
                                    final Long semesterId,
                                    final RecruitType recruitType,
                                    final Pageable pageable);
}
