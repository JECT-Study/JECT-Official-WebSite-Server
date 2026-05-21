package org.ject.support.domain.member.dto;

import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;

public record MemberAccountProjection(
        Long id,
        String email,
        String name,
        Role role,
        MemberStatus status
) {
}
