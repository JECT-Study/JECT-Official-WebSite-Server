package org.ject.support.domain.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.domain.admin.dto.AdminAuthSendRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendResponse;
import org.ject.support.domain.admin.dto.AdminVerifyRequest;
import org.ject.support.domain.admin.service.AdminAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController implements AdminAuthApiSpec {

    private final AdminAuthService adminAuthService;
    private final CustomSuccessHandler customSuccessHandler;

    @PostMapping("/auth/codes")
    public AdminAuthSendResponse sendAdminAuthCode(@RequestBody @Valid AdminAuthSendRequest request) {
        String email = adminAuthService.sendAdminAuthCode(request.email());
        return AdminAuthSendResponse.builder()
                .email(email)
                .build();
    }

    @PostMapping("/auth/codes/verify")
    public boolean verifyAdminAuthCode(
            @RequestBody @Valid AdminVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = adminAuthService.verifyAdminAuthCode(request.email(), request.code());
        customSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authentication);
        return true;
    }
}
