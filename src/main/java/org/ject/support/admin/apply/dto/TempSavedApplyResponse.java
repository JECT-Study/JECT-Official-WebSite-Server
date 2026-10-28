package org.ject.support.admin.apply.dto;

import org.ject.support.domain.member.CareerDetails;

public record TempSavedApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        CareerDetails careerDetails
) {
    public static TempSavedApplyResponse from(TempSavedApplyProjection projection) {
        return new TempSavedApplyResponse(
                projection.applyId(),
                projection.name(),
                projection.phoneNumber(),
                projection.careerDetails()
        );
    }
}
