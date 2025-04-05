package org.ject.support.domain.recruit.dto;

import java.util.List;

public record SemesterRegistered(Long semesterId, List<RecruitRegisterRequest> recruitRegisterRequests) {
}
