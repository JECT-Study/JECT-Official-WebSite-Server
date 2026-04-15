package org.ject.support.admin.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * 테스트 메일 발송 요청 DTO입니다.
 */
public record MailTestSendRequest(
        @NotNull(message = "시나리오 ID는 필수입니다.")
        Long mailScenarioId,
        @NotNull(message = "수신자 ID는 필수입니다.")
        Long receiverId,
        @NotBlank(message = "테스트 발송 대상 이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        String toEmail,
        @NotNull(message = "공통 변수 맵은 null일 수 없습니다.")
        Map<String, String> commonVariables
) {
}
