package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import java.time.LocalDateTime;

public record ActiveRecruitmentResponse(
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
        String recruitTypeDescription,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        RecruitTypeDetail recruitTypeDetail,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String recruitTypeDetailDescription,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        JobFamily jobFamily,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String jobFamilyDescription,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime startDate,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime endDate,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String summary
) {

    public static ActiveRecruitmentResponse from(Recruit recruit) {
        RecruitType recruitType = recruit.getRecruitType();
        RecruitTypeDetail recruitTypeDetail = recruit.getRecruitTypeDetail();
        return new ActiveRecruitmentResponse(
                recruit.getId(),
                recruit.getTitle(),
                recruit.getSemester() == null ? null : recruit.getSemester().getId(),
                recruit.getSemester() == null ? null : recruit.getSemester().getName(),
                recruitType,
                recruitType.getDescription(),
                recruitTypeDetail,
                recruitTypeDetail.getDescription(),
                recruit.getJobFamily(),
                recruit.getJobFamily().getDescription(),
                recruit.getStartDate(),
                recruit.getEndDate(),
                recruit.getSummary()
        );
    }
}
