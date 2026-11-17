package org.ject.support.admin.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.applicant.dto.ApplicantAccountProjection;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;

public record AdminAccountResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Role role,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        MemberStatus status
) {

    public static AdminAccountResponse from(final ApplicantAccountProjection projection) {
        return new AdminAccountResponse(
                projection.id(),
                projection.email(),
                projection.name(),
                projection.role(),
                projection.status());
    }
}
