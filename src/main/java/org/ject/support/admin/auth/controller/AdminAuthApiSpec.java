package org.ject.support.admin.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.ject.support.admin.auth.dto.AdminLoginRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin", description = "관리자 API")
public interface AdminAuthApiSpec {

    @Operation(
            summary = "관리자 로그인",
            description = "관리자 로그인을 시도합니다."
    )
    boolean loginAdmin(
            @RequestBody @Valid AdminLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "관리자 로그아웃",
            description = "관리자가 로그아웃합니다."
    )
    void logoutAdmin(HttpServletResponse httpResponse);
}
