package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.member.JobFamily;

import java.time.LocalDateTime;

public record RecruitUpdatedEvent(Long recruitId, JobFamily jobFamily, LocalDateTime startDate, LocalDateTime endDate) {
}
