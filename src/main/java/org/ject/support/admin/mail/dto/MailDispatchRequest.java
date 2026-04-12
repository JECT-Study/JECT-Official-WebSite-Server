package org.ject.support.admin.mail.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 메일 발송 작업 생성 요청 DTO입니다.
 */
public record MailDispatchRequest(
        @NotNull(message = "시나리오 ID는 필수입니다.")
        Long mailScenarioId,
        @NotEmpty(message = "수신자 ID 목록은 최소 1개 이상이어야 합니다.")
        List<@NotNull(message = "수신자 ID는 null일 수 없습니다.") Long> receiverIds,
        @NotNull(message = "공통 변수 맵은 null일 수 없습니다.")
        Map<String, String> commonVariables
) {
}
