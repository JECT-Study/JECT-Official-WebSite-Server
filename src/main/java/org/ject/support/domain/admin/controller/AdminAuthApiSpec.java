package org.ject.support.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.domain.admin.dto.AdminVerifyEmailRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendEmailRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendEmailResponse;
import org.ject.support.domain.admin.dto.AdminVerifyEmailResponse;
import org.ject.support.domain.admin.dto.AdminVerifySlackRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin", description = "관리자 API")
public interface AdminAuthApiSpec {

    @Operation(
            summary = "관리자 인증 이메일 코드 전송",
            description = "관리자 인증 코드를 이메일로 전송합니다."
    )
    AdminAuthSendEmailResponse sendAdminAuthEmailCode(@RequestBody @Valid AdminAuthSendEmailRequest request);

    @Operation(
            summary = "관리자 인증 이메일 코드 검증",
            description = "관리자 인증 이메일 코드를 검증합니다."
    )
    AdminVerifyEmailResponse verifyAdminAuthEmailCode(@RequestBody @Valid AdminVerifyEmailRequest request);

    @Operation(
            summary = "관리자 인증 Slack 코드 검증",
            description = "관리자 인증 Slack 코드를 검증합니다."
    )
    boolean verifyAdminAuthSlackCode(@RequestBody @Valid AdminVerifySlackRequest request);
}
