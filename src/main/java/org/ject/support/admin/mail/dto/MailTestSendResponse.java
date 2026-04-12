package org.ject.support.admin.mail.dto;

/**
 * 테스트 메일 발송 응답 DTO입니다.
 */
public record MailTestSendResponse(
        Long mailScenarioId,
        Long receiverId,
        String toEmail,
        String subject,
        String status
) {
}
