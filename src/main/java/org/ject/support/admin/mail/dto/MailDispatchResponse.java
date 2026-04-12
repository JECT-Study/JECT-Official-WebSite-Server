package org.ject.support.admin.mail.dto;

/**
 * 메일 발송 작업 생성 응답 DTO입니다.
 */
public record MailDispatchResponse(
        Long dispatchJobId,
        Long mailScenarioId,
        String status,
        int receiverCount
) {
}
