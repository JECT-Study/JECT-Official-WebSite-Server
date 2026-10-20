package org.ject.support.admin.apply.dto;

import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;

public record SubmittedApplyProjection(
        Long applyId,
        String name,
        String phoneNumber,
        CareerDetails careerDetails,
        SelectionResult selectionResult,
        Integer waitlistNumber,
        String note
) {
}
