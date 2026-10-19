package org.ject.support.domain.recruit.repository;

import java.util.List;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitProjection;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitSearchCondition;
import org.ject.support.domain.recruit.dto.RecruitTypeCountResponse;

public interface RecruitQueryRepository {

    List<ApplyManagementRecruitProjection> searchApplyManagementRecruits(
            ApplyManagementRecruitSearchCondition condition,
            int limit
    );

    long countApplyManagementRecruits(ApplyManagementRecruitSearchCondition condition);

    List<RecruitTypeCountResponse> countRecruitsByRecruitType();
}
