package org.ject.support.admin.apply.repository;

import java.util.Optional;
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminApplyQueryRepository {
    Page<Apply> findApplies(final AdminApplySearchCondition condition,
                            final Pageable pageable);

    Optional<Apply> findApplyByIdByStatus(final Long applyId, final ApplyStatus status);
}
