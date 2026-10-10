package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ScheduleMailDispatchRequest(
        @NotNull @Positive Long recruitId,
        @NotNull @Positive Long scenarioId,
        @NotEmpty @Size(max = 500) List<@NotNull @Positive Long> applyIds,
        String subjectOverride,
        Map<String, String> inputVariables,
        @NotNull
        @Schema(description = "시간대가 포함된 예약 시각. 한국 시간은 +09:00으로 입력",
                example = "2026-10-07T10:00:00+09:00")
        OffsetDateTime scheduledAt
) {
}
