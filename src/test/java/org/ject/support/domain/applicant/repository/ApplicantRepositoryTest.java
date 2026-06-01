package org.ject.support.domain.applicant.repository;

import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ApplicantRepositoryTest {

    @Autowired
    ApplicantRepository applicantRepository;

    @Test
    void 이메일과_역활로_지원자를_조회시_존재하는_지원자를_반환한다() {
        // given
        String email = "test@example.com";
        Role role = Role.ADMIN;
        Applicant applicant = createApplicant("테스트", "01012345678", email, JobFamily.FE, role);
        applicantRepository.save(applicant);

        // when
        Optional<Applicant> found = applicantRepository.findByEmailAndRole(email, role);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getRole()).isEqualTo(role);
    }

    @Test
    void 존재하지_않는_이메일과_역활로_지원자를_조회시_빈_Optional을_반환한다() {
        // given
        String findEmail = "notfound@example.com";
        Role findRole = Role.ADMIN;

        // when
        Optional<Applicant> found = applicantRepository.findByEmailAndRole(findEmail, findRole);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 이메일과_역할목록으로_지원자를_조회시_목록에_포함된_역할이면_지원을_반환한다() {
        // given
        String email = "backoffice@example.com";
        Applicant applicant = createApplicant("백오피스", "01087654321", email, JobFamily.FE, Role.OPERATIONS);
        applicantRepository.save(applicant);

        // when
        Optional<Applicant> found = applicantRepository.findByEmailAndRoleIn(
                email,
                List.of(Role.ADMIN, Role.OPERATIONS)
        );

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getRole()).isEqualTo(Role.OPERATIONS);
    }

    @Test
    void 이메일과_역할목록으로_지원자를_조회시_목록에_없는_역할이면_빈_Optional을_반환한다() {
        // given
        String email = "semester@example.com";
        Applicant applicant = createApplicant("지원자", "01099998888", email, JobFamily.BE, Role.SEMESTER);
        applicantRepository.save(applicant);

        // when
        Optional<Applicant> found = applicantRepository.findByEmailAndRoleIn(
                email,
                List.of(Role.ADMIN, Role.OPERATIONS, Role.SUPPORTER)
        );

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void id와_역할목록으로_지원자를_조회시_목록에_포함된_역할이면_지원자를_반환한다() {
        // given
        Applicant applicant = createApplicant("운영진", "01011112222", "operations@example.com", JobFamily.PM, Role.OPERATIONS);
        Applicant savedApplicant = applicantRepository.save(applicant);

        // when
        Optional<Applicant> found = applicantRepository.findByIdAndRoleIn(
                savedApplicant.getId(),
                List.of(Role.ADMIN, Role.OPERATIONS, Role.SUPPORTER)
        );

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedApplicant.getId());
        assertThat(found.get().getRole()).isEqualTo(Role.OPERATIONS);
    }

    @Test
    void id와_역할목록으로_지원자를_조회시_목록에_없는_역할이면_빈_Optional을_반환한다() {
        // given
        Applicant applicant = createApplicant("지원자", "01033334444", "member@example.com", JobFamily.BE, Role.SEMESTER);
        Applicant savedApplicant = applicantRepository.save(applicant);

        // when
        Optional<Applicant> found = applicantRepository.findByIdAndRoleIn(
                savedApplicant.getId(),
                List.of(Role.ADMIN, Role.OPERATIONS, Role.SUPPORTER)
        );

        // then
        assertThat(found).isEmpty();
    }

    private Applicant createApplicant(String name, String phoneNumber, String email, JobFamily jobFamily, Role role) {
        return Applicant.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .semesterId(1L)
                .jobFamily(jobFamily)
                .role(role)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
