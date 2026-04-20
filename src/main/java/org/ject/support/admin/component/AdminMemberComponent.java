package org.ject.support.admin.component;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberComponent {

    private final MemberRepository memberRepository;

    public Member getRequiredBackofficeMemberByEmail(String email) {
        return memberRepository.findByEmailAndRoleIn(email, Role.backofficeRoles())
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));
    }

    public Member getRequiredBackofficeMemberById(Long id) {
        return memberRepository.findByIdAndRoleIn(id, Role.backofficeRoles())
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));
    }

    public Optional<Member> findBackofficeMemberByEmail(String email) {
        return memberRepository.findByEmailAndRoleIn(email, Role.backofficeRoles());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeMemberStatus(Member member, MemberStatus status) {
        if (member.getStatus() == status)  {
            return;
        }
        member.edit(member.toEditor()
                .status(status)
                .build());
        memberRepository.save(member);
    }
}
