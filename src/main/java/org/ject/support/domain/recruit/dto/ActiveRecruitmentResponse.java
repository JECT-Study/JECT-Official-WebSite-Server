package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import java.time.LocalDateTime;

public record ActiveRecruitmentResponse(Long recruitId,
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
                                        String summary) {

    public static ActiveRecruitmentResponse from(Recruit recruit) {
        RecruitType recruitType = recruit.getRecruitType();
        RecruitTypeDetail recruitTypeDetail = recruit.getRecruitTypeDetail();
        return new ActiveRecruitmentResponse(
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
                recruit.getSummary()
        );
    }
}
