package org.ject.support.admin.account.dto;

import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;

import java.util.List;

public record AdminAccountSearchCondition(
        List<Role> roles,
        List<MemberStatus> statuses
) {
}
