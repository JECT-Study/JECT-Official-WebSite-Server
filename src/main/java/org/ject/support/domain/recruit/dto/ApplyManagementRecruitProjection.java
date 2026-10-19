package org.ject.support.domain.recruit.dto;

import java.time.LocalDateTime;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;

public record ApplyManagementRecruitProjection(
        Long recruitId,
        Long semesterId,
        String semesterName,
        RecruitType recruitType,
        JobFamily jobFamily,
        LocalDateTime startDate,
        LocalDateTime endDate,
        long submittedApplyCount,
        long passedCount,
        long failedCount,
        long waitlistedCount
) {
}
