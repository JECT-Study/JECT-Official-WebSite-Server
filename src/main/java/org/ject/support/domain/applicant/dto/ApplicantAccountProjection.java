package org.ject.support.domain.applicant.dto;

import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;

public record ApplicantAccountProjection(
        Long id,
        String email,
        String name,
        Role role,
        MemberStatus status
) {
}
