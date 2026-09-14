package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.common.security.AuthPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "AdminMailDispatch", description = "단체 메일 발송 API (어드민 전용)")
public interface AdminMailDispatchApiSpec {

    @Operation(summary = "단체 메일 발송", description = "선택한 제출 지원자에게 메일을 발송하고 대상별 결과를 기록합니다.")
    MailDispatchResponse sendMail(
            @Parameter(hidden = true) @AuthPrincipal Long requestedByAdminId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid SendMailDispatchRequest request);
}
