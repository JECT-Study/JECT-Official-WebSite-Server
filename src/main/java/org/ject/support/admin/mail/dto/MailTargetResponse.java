package org.ject.support.admin.mail.dto;

import org.ject.support.domain.apply.domain.SelectionResult;

public record MailTargetResponse(
        Long applyId,
        String name,
        String phoneNumber,
        String email,
        SelectionResult selectionResult,
        Integer waitlistNumber
) {
    public MailTargetResponse {
        if (selectionResult != SelectionResult.WAITLISTED) {
            waitlistNumber = null;
        }
    }
}
