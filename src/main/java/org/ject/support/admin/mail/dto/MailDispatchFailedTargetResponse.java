package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;

/**
 * 메일 발송 실패 대상 조회 응답 DTO입니다.
 */
public record MailDispatchFailedTargetResponse(
        Long targetId,
        Long receiverId,
        String email,
        String failureReason,
        LocalDateTime failedAt
) {
}
