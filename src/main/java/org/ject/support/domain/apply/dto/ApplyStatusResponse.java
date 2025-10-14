package org.ject.support.domain.apply.dto;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Apply.Status;

public record ApplyStatusResponse(
        Status status
) {
    public static ApplyStatusResponse of(Apply apply) {
        return new ApplyStatusResponse(apply.getStatus());
    }
}