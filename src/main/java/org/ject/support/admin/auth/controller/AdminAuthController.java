package org.ject.support.admin.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.auth.dto.AdminLoginRequest;
import org.ject.support.admin.auth.dto.AdminLoginResponse;
import org.ject.support.admin.auth.dto.AdminLoginResult;
import org.ject.support.admin.auth.service.AdminAuthService;
import org.ject.support.common.security.CustomSuccessHandler;
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

    @Override
    @PostMapping("/login")
    public AdminLoginResponse loginAdmin(@RequestBody @Valid AdminLoginRequest request,
                                HttpServletRequest httpRequest, HttpServletResponse response) {
        AdminLoginResult result = adminAuthService.authenticateAdmin(request.email(), request.password());
        customSuccessHandler.onAuthenticationSuccess(httpRequest, response, result.authentication());
        return new AdminLoginResponse(result.name(), result.isAdmin());
    }

    @PostMapping("/logout")
    public void logoutAdmin(HttpServletResponse httpResponse) {
        customSuccessHandler.onLogoutSuccess(httpResponse);
    }
}
