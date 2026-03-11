package org.ject.support.domain.apply.dto;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;

public record ApplyStatusResponse(
        ApplyStatus status
) {
    public static ApplyStatusResponse of(Apply apply) {
        return new ApplyStatusResponse(apply.getStatus());
    }
}
