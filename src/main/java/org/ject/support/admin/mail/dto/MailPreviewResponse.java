package org.ject.support.admin.mail.dto;

/**
 * 메일 미리보기 응답 DTO입니다.
 */
public record MailPreviewResponse(
        Long mailScenarioId,
        Long receiverId,
        String receiverEmail,
        String subject,
        String body
) {
}
