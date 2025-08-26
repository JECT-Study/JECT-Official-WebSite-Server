package org.ject.support.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.domain.admin.dto.AdminAuthSendSlackRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendSlackResponse;
import org.ject.support.domain.admin.dto.AdminVerifySlackRequest;
import org.ject.support.domain.admin.dto.AdminVerifySlackResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin", description = "관리자 API")
public interface AdminAuthApiSpec {

    @Operation(
            summary = "관리자 인증 Slack 코드 전송",
            description = "관리자 인증 코드를 Slack으로 전송합니다."
    )
    AdminAuthSendSlackResponse sendAdminAuthSlackCode(@RequestBody @Valid AdminAuthSendSlackRequest request);

    @Operation(
            summary = "관리자 인증 Slack 코드 검증",
            description = "관리자 인증 Slack 코드를 검증합니다."
    )
    AdminVerifySlackResponse verifyAdminAuthSlackCode(@RequestBody @Valid AdminVerifySlackRequest request);
}
