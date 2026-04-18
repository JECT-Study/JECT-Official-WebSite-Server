package org.ject.support.admin.account.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountActiveUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.dto.AdminAccountRoleUpdateRequest;
import org.ject.support.admin.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberEditor;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

    private final MemberRepository memberRepository;
    private final AdminMemberComponent adminMemberComponent;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAccount(final AdminAccountCreateRequest request) {
        validateBackofficeRole(request.role());
        validateEmailUniqueness(request);

        final String encodedPassword = passwordEncoder.encode(request.password());
        memberRepository.save(request.toEntity(encodedPassword));
    }

    @Transactional
    public void updateRole(final Long memberId, final AdminAccountRoleUpdateRequest request) {
        validateBackofficeRole(request.role());

        final Member member = adminMemberComponent.getRequiredBackofficeMemberById(memberId);

        final MemberEditor editor = member.toEditor()
                .role(request.role())
                .build();

        member.edit(editor);
    }

    @Transactional
    public void updateActive(final Long requesterId,
                             final Long memberId,
                             final AdminAccountActiveUpdateRequest request) {
        validateNotLockingSelf(requesterId, memberId, request.active());

        final Member member = adminMemberComponent.getRequiredBackofficeMemberById(memberId);
        final MemberStatus status = request.active() ? MemberStatus.ACTIVE : MemberStatus.LOCKED;

        adminMemberComponent.changeMemberStatus(member, status);
    }

    private void validateNotLockingSelf(final Long requesterId,
                                        final Long memberId,
                                        final boolean active) {
        if (!active && requesterId.equals(memberId)) {
            throw new AdminException(AdminErrorCode.CANNOT_LOCK_SELF);
        }
    }

    private void validateBackofficeRole(final Role role) {
        if (!role.isBackoffice()) {
            throw new AdminException(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);
        }
    }

    private void validateEmailUniqueness(final AdminAccountCreateRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AdminException(AdminErrorCode.DUPLICATE_ADMIN_EMAIL);
        }
    }
}
