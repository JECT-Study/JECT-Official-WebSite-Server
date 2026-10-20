package org.ject.support.domain.apply.dto;

import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;

public record ApplyEligibilityFacts(
        Long applyId,
        Long recruitId,
        ApplyStatus status,
        SelectionResult selectionResult
) {
}
