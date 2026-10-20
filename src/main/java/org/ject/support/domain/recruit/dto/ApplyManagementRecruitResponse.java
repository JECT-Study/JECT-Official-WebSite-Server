package org.ject.support.domain.recruit.dto;

import java.time.LocalDateTime;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;

public record ApplyManagementRecruitResponse(
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
    public static ApplyManagementRecruitResponse from(ApplyManagementRecruitProjection projection) {
        RecruitType recruitType = projection.recruitType();
        JobFamily jobFamily = projection.jobFamily();
        boolean isSemesterRecruit = recruitType == RecruitType.SEMESTER;
        return new ApplyManagementRecruitResponse(
                projection.recruitId(),
                isSemesterRecruit ? projection.semesterId() : null,
                isSemesterRecruit ? projection.semesterName() : null,
                recruitType,
                jobFamily,
                projection.startDate(),
                projection.endDate(),
                projection.submittedApplyCount(),
                projection.passedCount(),
                projection.failedCount(),
                projection.waitlistedCount()
        );
    }
}
