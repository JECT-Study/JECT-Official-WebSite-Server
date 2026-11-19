package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * 메일 미리보기 응답 DTO입니다.
 */
public record MailPreviewResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long scenarioId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long applyId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String receiverEmail,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String subject,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String body
) {
}
