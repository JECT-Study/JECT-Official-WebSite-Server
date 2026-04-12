package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;

/**
 * 메일 발송 작업 이력 조회 응답 DTO입니다.
 */
public record MailDispatchHistoryResponse(
        Long dispatchJobId,
        Long mailScenarioId,
        String scenarioCode,
        String scenarioName,
        String status,
        int receiverCount,
        int sentCount,
        int failedCount,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
