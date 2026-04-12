package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 메일 발송 작업 상세 조회 응답 DTO입니다.
 */
public record MailDispatchDetailResponse(
        Long dispatchJobId,
        Long mailScenarioId,
        String scenarioCode,
        String scenarioName,
        String status,
        int receiverCount,
        int pendingCount,
        int sentCount,
        int failedCount,
        Map<String, String> commonVariables,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
