package org.ject.support.admin.apply.dto;

import org.ject.support.domain.member.CareerDetails;

public record TempSavedApplyProjection(
        Long applyId,
        String name,
        String phoneNumber,
        CareerDetails careerDetails
) {
}
