package org.ject.support.admin.apply.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record EditWaitlistOrderRequest(
        @NotNull @Positive Long recruitId,
        @NotEmpty List<@NotNull @Positive Long> applyIds
) {
}
