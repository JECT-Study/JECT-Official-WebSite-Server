package org.ject.support.domain.member.event;

import org.ject.support.domain.member.entity.Member;

public record TempMemberRegisteredEvent(Long memberId) {
    public static TempMemberRegisteredEvent of(Member member) {
        return new TempMemberRegisteredEvent(member.getId());
    }
}
