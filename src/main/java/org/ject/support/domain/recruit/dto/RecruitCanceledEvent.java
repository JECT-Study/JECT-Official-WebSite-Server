package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.member.JobFamily;

public record RecruitCanceledEvent(Long recruitId, JobFamily jobFamily) {
}