package org.ject.support.domain.member.entity;

import lombok.Builder;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;

@Builder
public record MemberEditor (
        String name,
        String phoneNumber,
        String email,
        Long semesterId,
        JobFamily jobFamily,
        Role role
){
}