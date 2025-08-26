package org.ject.support.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.dto.AdminAuthSendSlackRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendSlackResponse;
import org.ject.support.domain.admin.dto.AdminVerifySlackRequest;
import org.ject.support.domain.admin.dto.AdminVerifySlackResponse;
import org.ject.support.domain.admin.service.AdminAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController implements AdminAuthApiSpec {

    private final AdminAuthService adminAuthService;

    @PostMapping("/auth/slack-codes")
    public AdminAuthSendSlackResponse sendAdminAuthSlackCode(@RequestBody @Valid AdminAuthSendSlackRequest request) {
        String email = adminAuthService.sendSlackAdminAuthCode(request.email());
        return AdminAuthSendSlackResponse.builder()
                .email(email)
                .build();
    }

    @PostMapping("/auth/slack-codes/verify")
    public AdminVerifySlackResponse verifyAdminAuthSlackCode(@RequestBody @Valid AdminVerifySlackRequest request) {
        // TODO : 관리자 인증 Slack 코드 검증 로직 구현, accessToken 발급
        return AdminVerifySlackResponse.builder()
                .id("1")
                .email(request.email())
                .name("관리자")
                .build();
    }
}
