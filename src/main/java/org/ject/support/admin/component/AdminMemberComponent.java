package org.ject.support.admin.component;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberComponent {

    private final ApplicantRepository applicantRepository;

    // TODO: 내부 주체는 Applicant지만 관리자 계정 관리 맥락상 Member 네이밍을 유지한다.
    public Applicant getRequiredBackofficeMemberByEmail(String email) {
        return applicantRepository.findByEmailAndRoleIn(email, Role.backofficeRoles())
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));
    }

    public Applicant getRequiredBackofficeMemberById(Long id) {
        return applicantRepository.findByIdAndRoleIn(id, Role.backofficeRoles())
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));
    }

    public Optional<Applicant> findBackofficeMemberByEmail(String email) {
        return applicantRepository.findByEmailAndRoleIn(email, Role.backofficeRoles());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeMemberStatus(Applicant applicant, MemberStatus status) {
        if (applicant.getStatus() == status)  {
            return;
        }
        applicant.edit(applicant.toEditor()
                .status(status)
                .build());
        applicantRepository.save(applicant);
    }
}
