package org.ject.support.admin.mail.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;

/**
 * 메일 미리보기 요청 DTO입니다.
 */
public record PreviewMailRequest(
        @NotNull(message = "시나리오 ID는 필수입니다.")
        @Positive(message = "시나리오 ID는 양수여야 합니다.")
        Long scenarioId,
        @NotNull(message = "지원 ID는 필수입니다.")
        @Positive(message = "지원 ID는 양수여야 합니다.")
        Long applyId,
        @NotNull(message = "입력 변수 맵은 null일 수 없습니다.")
        Map<String, String> inputVariables
) {
}
