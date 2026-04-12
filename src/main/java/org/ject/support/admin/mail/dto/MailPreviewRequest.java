package org.ject.support.admin.mail.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * 메일 미리보기 요청 DTO입니다.
 */
public record MailPreviewRequest(
        @NotNull(message = "시나리오 ID는 필수입니다.")
        Long mailScenarioId,
        @NotNull(message = "수신자 ID는 필수입니다.")
        Long receiverId,
        @NotNull(message = "공통 변수 맵은 null일 수 없습니다.")
        Map<String, String> commonVariables
) {
}
