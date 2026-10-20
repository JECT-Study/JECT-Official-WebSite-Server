package org.ject.support.admin.apply.dto;

import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;

public record SubmittedApplyResponse(
        Long applyId,
        String name,
        String phoneNumber,
        CareerDetails careerDetails,
        SelectionResult selectionResult,
        Integer waitlistNumber,
        String note
) {
    public static SubmittedApplyResponse from(SubmittedApplyProjection projection) {
        return new SubmittedApplyResponse(
                projection.applyId(),
                projection.name(),
                projection.phoneNumber(),
                projection.careerDetails(),
                projection.selectionResult(),
                projection.waitlistNumber(),
                projection.note()
        );
    }
}
