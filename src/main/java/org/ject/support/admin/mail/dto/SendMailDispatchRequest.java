package org.ject.support.admin.mail.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record SendMailDispatchRequest(
        @NotNull(message = "모집 공고 ID는 필수입니다.")
        @Positive(message = "모집 공고 ID는 양수여야 합니다.")
        Long recruitId,
        @NotNull(message = "시나리오 ID는 필수입니다.")
        @Positive(message = "시나리오 ID는 양수여야 합니다.")
        Long scenarioId,
        @NotEmpty(message = "메일 발송 대상은 비어 있을 수 없습니다.")
        @Size(max = 500, message = "메일 발송 대상은 500명을 초과할 수 없습니다.")
        List<@NotNull(message = "지원 ID는 null일 수 없습니다.") @Positive(message = "지원 ID는 양수여야 합니다.") Long> applyIds,
        String subjectOverride,
        Map<String, String> inputVariables
) {
}
