package org.ject.support.admin.account.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountCreateRequest;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAccount(final AdminAccountCreateRequest request) {
        validateBackofficeRole(request);
        validateEmailUniqueness(request);

        final String encodedPassword = passwordEncoder.encode(request.password());
        memberRepository.save(request.toEntity(encodedPassword));
    }

    private void validateBackofficeRole(final AdminAccountCreateRequest request) {
        if (!request.role().isBackoffice()) {
            throw new AdminException(AdminErrorCode.INVALID_ADMIN_ACCOUNT_ROLE);
        }
    }

    private void validateEmailUniqueness(final AdminAccountCreateRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AdminException(AdminErrorCode.DUPLICATE_ADMIN_EMAIL);
        }
    }
}
