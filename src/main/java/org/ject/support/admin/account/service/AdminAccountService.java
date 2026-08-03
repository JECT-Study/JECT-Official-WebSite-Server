package org.ject.support.admin.account.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountActiveUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.account.dto.AdminAccountRoleUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountResponse;
import org.ject.support.admin.account.dto.AdminAccountUpdateRequest;
import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.admin.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.entity.ApplicantEditor;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

    private final ApplicantRepository applicantRepository;
    private final AdminMemberComponent adminMemberComponent;
    private final PasswordEncoder passwordEncoder;

    public Page<AdminAccountResponse> findAccounts(final AdminAccountSearchCondition condition,
                                                   final Pageable pageable) {
        validateBackofficeRoles(condition.roles());
        return applicantRepository.findAccounts(condition, pageable)
                .map(AdminAccountResponse::from);
    }

    @Transactional
    public void createAccount(final AdminAccountCreateRequest request) {
        validateBackofficeRole(request.role());
        validateEmailUniqueness(request);

        final String encodedPassword = passwordEncoder.encode(request.password());
        applicantRepository.save(request.toEntity(encodedPassword));
    }

    @Transactional
    public void updateAccount(final Long requesterId,
                              final Long memberId,
                              final AdminAccountUpdateRequest request) {
        validateBackofficeRole(request.role());
        validateNotLockingSelf(requesterId, memberId, request.active());
        validateNotChangingOwnAdminRole(requesterId, memberId, request.role());

        final Applicant applicant = adminMemberComponent.getRequiredBackofficeMemberById(memberId);

        final MemberStatus status = request.active() ? MemberStatus.ACTIVE : MemberStatus.LOCKED;
        final ApplicantEditor editor = applicant.toEditor()
                .name(request.normalizeName())
                .role(request.role())
                .status(status)
                .build();

        applicant.edit(editor);
    }

    @Transactional
    public void updateRole(final Long memberId, final AdminAccountRoleUpdateRequest request) {
        validateBackofficeRole(request.role());

        final Applicant applicant = adminMemberComponent.getRequiredBackofficeMemberById(memberId);

        final ApplicantEditor editor = applicant.toEditor()
                .role(request.role())
                .build();

        applicant.edit(editor);
    }

    @Transactional
    public void updateActive(final Long requesterId,
                             final Long memberId,
                             final AdminAccountActiveUpdateRequest request) {
        validateNotLockingSelf(requesterId, memberId, request.active());

        final Applicant applicant = adminMemberComponent.getRequiredBackofficeMemberById(memberId);
        final MemberStatus status = toMemberStatus(request.active());

        adminMemberComponent.changeMemberStatus(applicant, status);
    }

    @Transactional
    public void updateActive(final Long requesterId,
                             final List<AdminAccountActiveUpdateRequest> requests) {
        requests.forEach(request -> validateBulkUpdateRequest(requesterId, request));

        final List<Applicant> applicants = requests.stream()
                .map(request -> adminMemberComponent.getRequiredBackofficeMemberById(request.memberId()))
                .toList();

        adminMemberComponent.changeMemberStatuses(applicants, MemberStatus.LOCKED);
    }

    private MemberStatus toMemberStatus(final Boolean active) {
        return active ? MemberStatus.ACTIVE : MemberStatus.LOCKED;
    }

    private void validateNotLockingSelf(final Long requesterId,
                                        final Long memberId,
                                        final boolean active) {
        if (!active && requesterId.equals(memberId)) {
            throw new AdminException(AdminErrorCode.CANNOT_LOCK_SELF);
        }
    }

    private void validateBulkUpdateRequest(final Long requesterId,
                                           final AdminAccountActiveUpdateRequest request) {
        if (request == null || request.memberId() == null) {
            throw new AdminException(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ID);
        }
        if (!Boolean.FALSE.equals(request.active())) {
            throw new AdminException(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ACTIVE);
        }
        validateNotLockingSelf(requesterId, request.memberId(), request.active());
    }

    private void validateNotChangingOwnAdminRole(final Long requesterId,
                                                final Long memberId,
                                                final Role role) {
        if (requesterId.equals(memberId) && role != Role.ADMIN) {
            throw new AdminException(AdminErrorCode.CANNOT_CHANGE_OWN_ADMIN_ROLE);
        }
    }

    private void validateBackofficeRole(final Role role) {
        if (!role.isBackoffice()) {
            throw new AdminException(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);
        }
    }

    private void validateBackofficeRoles(final List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        if (roles.stream().anyMatch(role -> !role.isBackoffice())) {
            throw new AdminException(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);
        }
    }

    private void validateEmailUniqueness(final AdminAccountCreateRequest request) {
        if (applicantRepository.existsByEmail(request.email())) {
            throw new AdminException(AdminErrorCode.DUPLICATE_ADMIN_EMAIL);
        }
    }

}
