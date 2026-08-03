package org.ject.support.admin.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountResponse;
import org.ject.support.admin.account.dto.AdminAccountActiveUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.dto.AdminAccountRoleUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.admin.account.service.AdminAccountService;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/accounts")
@Tag(name = "Admin Account", description = "관리자 계정 관리 API")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 목록 조회",
            description = "관리자 계정 목록을 조회합니다. 계정 유형과 상태를 복수 필터로 선택할 수 있습니다.")
    public Page<AdminAccountResponse> findAccounts(
            @RequestParam(required = false) final List<Role> roles,
            @RequestParam(required = false) final List<MemberStatus> statuses,
            @PageableDefault(sort = "createdAt", direction = Direction.DESC) final Pageable pageable) {
        AdminAccountSearchCondition condition = new AdminAccountSearchCondition(roles, statuses);
        return adminAccountService.findAccounts(condition, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 생성",
            description = "관리자 계정 유형의 계정을 생성합니다.")
    public void createAccount(@RequestBody @Valid final AdminAccountCreateRequest request) {
        adminAccountService.createAccount(request);
    }

    @PatchMapping("/{memberId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 정보 수정",
            description = "관리자 계정의 이메일, 이름, 권한, 활성화 상태를 수정합니다.")
    public void updateAccount(@Parameter(hidden = true) @AuthPrincipal final Long requesterId,
                              @PathVariable final Long memberId,
                              @RequestBody @Valid final AdminAccountUpdateRequest request) {
        adminAccountService.updateAccount(requesterId, memberId, request);
    }

    @PatchMapping("/{memberId}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 권한 수정",
            description = "관리자 계정의 권한을 수정합니다.")
    public void updateRole(@PathVariable final Long memberId,
                           @RequestBody @Valid final AdminAccountRoleUpdateRequest request) {
        adminAccountService.updateRole(memberId, request);
    }

    @PatchMapping("/members/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 일괄 비활성화",
            description = "관리자 계정을 일괄 비활성화합니다.")
    public void updateActive(@Parameter(hidden = true) @AuthPrincipal final Long requesterId,
                             @RequestBody @Valid @NotEmpty final List<@NotNull @Valid AdminAccountActiveUpdateRequest> requests) {
        adminAccountService.updateActive(requesterId, requests);
    }

    @PatchMapping("/{memberId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "관리자 계정 활성화 상태 수정",
            description = "관리자 계정을 활성화하거나 비활성화합니다.")
    public void updateActive(@Parameter(hidden = true) @AuthPrincipal final Long requesterId,
                             @PathVariable final Long memberId,
                             @RequestBody @Valid final AdminAccountActiveUpdateRequest request) {
        adminAccountService.updateActive(requesterId, memberId, request);
    }
}
