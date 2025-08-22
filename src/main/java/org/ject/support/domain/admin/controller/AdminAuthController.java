package org.ject.support.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.dto.AdminVerifyEmailRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendEmailRequest;
import org.ject.support.domain.admin.dto.AdminAuthSendEmailResponse;
import org.ject.support.domain.admin.dto.AdminVerifyEmailResponse;
import org.ject.support.domain.admin.dto.AdminVerifySlackRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController implements AdminAuthApiSpec {

    @PostMapping("/auth/email-codes")
    public AdminAuthSendEmailResponse sendAdminAuthEmailCode(@RequestBody @Valid AdminAuthSendEmailRequest request) {
        // TODO : 관리자 인증 코드 전송 로직 구현
        return AdminAuthSendEmailResponse.builder()
                .email("test@tset.com")
                .build();
    }

    @PostMapping("/auth/email-codes/verify")
    public AdminVerifyEmailResponse verifyAdminAuthEmailCode(@RequestBody @Valid AdminVerifyEmailRequest request) {
        // TODO : 관리자 인증 이메일 코드 검증 로직 구현, Slack 인증 코드 전송
        return AdminVerifyEmailResponse.builder()
                .email("test@tset.com")
                .build();
    }

    @PostMapping("/auth/slack-codes/verify")
    public boolean verifyAdminAuthSlackCode(@RequestBody @Valid AdminVerifySlackRequest request) {
        // TODO : 관리자 인증 Slack 코드 검증 로직 구현, accessToken 발급
        return false;
    }
}
