package org.ject.support.admin.apply.dto;

import org.ject.support.domain.member.CareerDetails;

public record WaitlistOrderResponse(
        Long applyId,
        String name,
        String phoneNumber,
        CareerDetails careerDetails,
        Integer waitlistNumber
) {
}
