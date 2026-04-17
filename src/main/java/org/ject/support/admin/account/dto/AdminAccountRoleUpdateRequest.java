package org.ject.support.admin.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.ject.support.domain.member.Role;

@Schema(description = "관리자 계정 권한 수정 요청")
public record AdminAccountRoleUpdateRequest(
        @Schema(
                description = "관리자 계정 유형",
                example = "SUPPORTER",
                allowableValues = {"ADMIN", "OPERATIONS", "SUPPORTER"})
        @NotNull
        Role role
) {
}
