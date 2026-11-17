package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;

public record ApplyManagementRecruitResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long recruitId,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Long semesterId,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String semesterName,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        RecruitType recruitType,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        JobFamily jobFamily,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime startDate,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime endDate,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        long submittedApplyCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        long passedCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        long failedCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        long waitlistedCount
) {
    public static ApplyManagementRecruitResponse from(ApplyManagementRecruitProjection projection) {
        RecruitType recruitType = projection.recruitType();
        JobFamily jobFamily = projection.jobFamily();
        boolean isSemesterRecruit = recruitType == RecruitType.SEMESTER;
        return new ApplyManagementRecruitResponse(
                projection.recruitId(),
                projection.title(),
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
