package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.recruit.domain.RecruitType;

public record RecruitTypeCountResponse(
        RecruitType recruitType,
        long count
) {
}
