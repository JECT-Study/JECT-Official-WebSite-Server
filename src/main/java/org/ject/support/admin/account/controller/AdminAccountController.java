package org.ject.support.admin.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.service.AdminAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/accounts")
@Tag(name = "Admin Account", description = "관리자 계정 관리 API")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 생성",
            description = "관리자 계정 유형의 계정을 생성합니다.")
    public void createAccount(@RequestBody @Valid final AdminAccountCreateRequest request) {
        adminAccountService.createAccount(request);
    }
}
