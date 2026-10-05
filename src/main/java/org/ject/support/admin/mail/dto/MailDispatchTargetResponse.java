package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;

public record MailDispatchTargetResponse(
        Long targetId,
        Long applyId,
        String email,
        MailDispatchTargetStatus status,
        LocalDateTime sentAt,
        String failureReason,
        @Schema(description = "기록된 발송 시도 횟수(claim 획득 기준). Outbox 없는 과거 이력은 null")
        Integer attemptCount,
        @Schema(description = "예약된 다음 재시도 시각. 대기 시각이 없으면 null이며 재발송 허가를 뜻하지 않음")
        LocalDateTime nextAttemptAt,
        @Schema(description = "가장 최근 저장된 시도 오류 코드. 대기 중 오류는 최종 실패 사유와 구분")
        String lastAttemptFailureReason
) {
}
