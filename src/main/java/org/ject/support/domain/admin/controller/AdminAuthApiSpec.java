package org.ject.support.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.ject.support.domain.admin.dto.AdminAuthSendRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendResponse;
import org.ject.support.domain.admin.dto.AdminVerifyRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin", description = "관리자 API")
public interface AdminAuthApiSpec {

    @Operation(
            summary = "관리자 로그인 인증 코드 전송",
            description = "관리자 로그인 인증 코드를 전송합니다."
    )
    AdminAuthSendResponse sendAdminAuthCode(@RequestBody @Valid AdminAuthSendRequest request);

    @Operation(
            summary = "관리자 로그인 인증 코드 검증",
            description = "관리자 로그인 인증 코드를 검증합니다."
    )
    boolean verifyAdminAuthCode(
            @RequestBody @Valid AdminVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );
}
