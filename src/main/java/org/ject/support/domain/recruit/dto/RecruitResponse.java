package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record RecruitResponse(
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
        String recruitInformation,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String notice,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<RecruitFaqResponse> faqs
) {

    // 모집공고 상세 응답 변환
    public static RecruitResponse from(Recruit recruit) {
        RecruitType recruitType = recruit.getRecruitType();
        RecruitTypeDetail recruitTypeDetail = recruit.getRecruitTypeDetail();
        return new RecruitResponse(
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
                recruit.getRecruitInformation(),
                recruit.getNotice(),
                recruit.getFaqs().stream().map(RecruitFaqResponse::from).toList()
        );
    }
}
