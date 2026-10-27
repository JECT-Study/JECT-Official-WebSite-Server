package org.ject.support.admin.apply.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WaitlistOrderSearchCondition(
        @NotNull @Positive Long recruitId,
        @Positive Long cursor,
        @Min(1) @Max(100) Integer size
) {
    public int getSizeOrDefault() {
        return size == null ? 20 : size;
    }
}
