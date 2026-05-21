package org.ject.support.admin.account.dto;

import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberAccountProjection;

public record AdminAccountResponse(
        Long id,
        String email,
        String name,
        Role role,
        MemberStatus status
) {

    public static AdminAccountResponse from(final MemberAccountProjection projection) {
        return new AdminAccountResponse(
                projection.id(),
                projection.email(),
                projection.name(),
                projection.role(),
                projection.status());
    }
}
