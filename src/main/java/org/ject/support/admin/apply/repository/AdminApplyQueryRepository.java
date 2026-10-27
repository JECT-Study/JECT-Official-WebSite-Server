package org.ject.support.admin.apply.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.apply.dto.SubmittedApplyProjection;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyProjection;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.admin.apply.dto.WaitlistOrderResponse;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;

public interface AdminApplyQueryRepository {

    List<WaitlistOrderResponse> getWaitlistOrders(Long recruitId);

    List<SubmittedApplyProjection> searchSubmittedApplies(
            SubmittedApplySearchCondition condition,
            int limit);

    long countSubmittedApplies(SubmittedApplySearchCondition condition);

    List<TempSavedApplyProjection> searchTempSavedApplies(
            TempSavedApplySearchCondition condition,
            int limit);

    long countTempSavedApplies(TempSavedApplySearchCondition condition);

    Optional<Apply> findApplyByIdByStatus(final Long applyId, final ApplyStatus status);
}
