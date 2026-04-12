package org.ject.support.admin.mail.dto;

/**
 * 메일 발송 작업 실행 결과 응답 DTO입니다.
 */
public record MailDispatchExecuteResponse(
        Long dispatchJobId,
        String status,
        int successCount,
        int failedCount
) {
}
