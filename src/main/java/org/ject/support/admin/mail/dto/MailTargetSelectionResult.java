package org.ject.support.admin.mail.dto;

import org.ject.support.domain.apply.domain.SelectionResult;

public enum MailTargetSelectionResult {
    PASSED, WAITLISTED, FAILED;

    public SelectionResult toSelectionResult() {
        return SelectionResult.valueOf(name());
    }
}
