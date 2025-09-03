package org.ject.support.domain.admin.component;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberComponent {

    private final MemberRepository memberRepository;

    public Member getMemberAdminByEmail(String email) {
        return memberRepository.findByEmailAndRole(email, Role.ADMIN)
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeMemberStatus(Member member, MemberStatus status) {
        member.setStatus(status);
        memberRepository.save(member);
    }
}
