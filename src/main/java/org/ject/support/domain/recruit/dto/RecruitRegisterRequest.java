package org.ject.support.domain.recruit.dto;

import java.time.LocalDateTime;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;

public record RecruitRegisterRequest(JobFamily jobFamily,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate) {
    public Recruit toEntity(Long ongoingSemesterId) {
        return Recruit.builder()
                .semesterId(ongoingSemesterId)
                .jobFamily(this.jobFamily)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}
