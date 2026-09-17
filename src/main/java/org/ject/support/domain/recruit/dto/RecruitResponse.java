package org.ject.support.domain.recruit.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record RecruitResponse(
        Long recruitId,
        Long semesterId,
        String semesterName,
        RecruitType recruitType,
        String recruitTypeDescription,
        RecruitTypeDetail recruitTypeDetail,
        String recruitTypeDetailDescription,
        JobFamily jobFamily,
        String jobFamilyDescription,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String recruitInformation,
        String notice,
        List<RecruitFaqResponse> faqs
) {

    // 모집공고 상세 응답 변환
    public static RecruitResponse from(Recruit recruit) {
        RecruitType recruitType = toPublicRecruitType(recruit.getRecruitType());
        RecruitTypeDetail recruitTypeDetail = toPublicRecruitTypeDetail(recruit);
        return new RecruitResponse(
                recruit.getId(),
                recruit.getSemester().getId(),
                recruit.getSemester().getName(),
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

    private static RecruitType toPublicRecruitType(RecruitType recruitType) {
        return switch (recruitType) {
            case REGULAR, REGULAR_WAITLIST, BACKFILL, MANUAL -> RecruitType.SEMESTER;
            case SEMESTER, MAKERS, SUPPORTERS -> recruitType;
        };
    }

    private static RecruitTypeDetail toPublicRecruitTypeDetail(Recruit recruit) {
        if (recruit.getRecruitType() == RecruitType.BACKFILL) {
            return RecruitTypeDetail.REFILL;
        }
        return recruit.getRecruitTypeDetail();
    }
}
