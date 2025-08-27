package org.ject.support.domain.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.domain.admin.dto.AdminAuthSendSlackRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendSlackResponse;
import org.ject.support.domain.admin.dto.AdminVerifySlackRequest;
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

    @PostMapping("/auth/slack-codes")
    public AdminAuthSendSlackResponse sendAdminAuthSlackCode(@RequestBody @Valid AdminAuthSendSlackRequest request) {
        String email = adminAuthService.sendSlackAdminAuthCode(request.email());
        return AdminAuthSendSlackResponse.builder()
                .email(email)
                .build();
    }

    @PostMapping("/auth/slack-codes/verify")
    public boolean verifyAdminAuthSlackCode(
            @RequestBody @Valid AdminVerifySlackRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = adminAuthService.verifySlackAdminAuthCode(request.email(), request.code());
        customSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authentication);
        return true;
    }
}
