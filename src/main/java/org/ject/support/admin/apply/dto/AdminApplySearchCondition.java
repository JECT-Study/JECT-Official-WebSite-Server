package org.ject.support.admin.apply.dto;

import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record AdminApplySearchCondition(
        ApplyStatus applyStatus,
        Long semesterId,
        JobFamily jobFamily,
        RecruitType recruitType,
        RecruitTypeDetail recruitTypeDetail,
        Long recruitId
) {
}
