package org.ject.support.admin.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.auth.dto.AdminAuthSendRequest;
import org.ject.support.admin.auth.dto.AdminAuthSendResponse;
import org.ject.support.admin.auth.dto.AdminLoginRequest;
import org.ject.support.admin.auth.dto.AdminVerifyRequest;
import org.ject.support.admin.auth.service.AdminAuthService;
import org.ject.support.common.security.CustomSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin-auth")
public class AdminAuthController implements AdminAuthApiSpec {

    private final AdminAuthService adminAuthService;
    private final CustomSuccessHandler customSuccessHandler;

    @PostMapping("/code")
    public AdminAuthSendResponse sendAdminAuthCode(@RequestBody @Valid AdminAuthSendRequest request) {
        String email = adminAuthService.sendAdminAuthCode(request.email());
        return AdminAuthSendResponse.builder()
                .email(email)
                .build();
    }

    @PostMapping("/verify-code")
    public boolean verifyAdminAuthCode(
            @RequestBody @Valid AdminVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = adminAuthService.verifyAdminAuthCode(request.email(), request.code());
        customSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authentication);
        return true;
    }

    @Override
    @PostMapping("/login")
    public boolean loginAdmin(@RequestBody @Valid AdminLoginRequest request,
                                HttpServletRequest httpRequest, HttpServletResponse response) {
        Authentication authentication = adminAuthService.authenticateAdmin(request.email(), request.password());
        customSuccessHandler.onAuthenticationSuccess(httpRequest, response, authentication);
        return true;
    }

    @PostMapping("/logout")
    public void logoutAdmin(HttpServletResponse httpResponse) {
        customSuccessHandler.onLogoutSuccess(httpResponse);
    }
}
