package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MailRecruitResponse(
        @Schema(description = "모집 공고 ID", example = "1")
        Long recruitId,
        @Schema(description = "기수 ID", example = "2")
        Long semesterId,
        @Schema(description = "기수명", example = "10기")
        String semesterName,
        @Schema(description = "직군", example = "BE")
        JobFamily jobFamily,
        @Schema(description = "직군 표시명", example = "백엔드 개발자(BE)")
        String jobFamilyDescription,
        @Schema(description = "모집 유형", example = "SEMESTER")
        RecruitType recruitType,
        @Schema(description = "모집 유형 표시명", example = "정규 기수 모집")
        String recruitTypeDescription,
        @Schema(description = "모집 세부 유형", example = "REGULAR")
        RecruitTypeDetail recruitTypeDetail,
        @Schema(description = "모집 세부 유형 표시명", example = "정규 모집")
        String recruitTypeDetailDescription,
        @Schema(description = "모집 시작일", example = "2026-08-01T00:00:00")
        LocalDateTime startDate,
        @Schema(description = "모집 종료일", example = "2026-08-31T23:59:00")
        LocalDateTime endDate,
        @Schema(description = "등록일", example = "2026-07-01T10:00:00")
        LocalDateTime createdAt
) {

    public static MailRecruitResponse from(Recruit recruit) {
        return new MailRecruitResponse(
                recruit.getId(),
                recruit.getSemester().getId(),
                recruit.getSemester().getName(),
                recruit.getJobFamily(),
                recruit.getJobFamily().getDescription(),
                recruit.getRecruitType(),
                recruit.getRecruitType().getDescription(),
                recruit.getRecruitTypeDetail(),
                recruit.getRecruitTypeDetail().getDescription(),
                recruit.getStartDate(),
                recruit.getEndDate(),
                recruit.getCreatedAt()
        );
    }
}
