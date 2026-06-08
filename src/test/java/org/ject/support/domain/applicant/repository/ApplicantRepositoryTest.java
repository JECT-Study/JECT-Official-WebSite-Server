package org.ject.support.domain.applicant.repository;

import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.ject.support.domain.apply.domain.ApplyStatus.JOINED;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.apply.domain.ApplyStatus.TEMP_SAVED;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ApplicantRepositoryTest {

    @Autowired
    ApplicantRepository applicantRepository;

    @Autowired
    ApplyRepository applyRepository;

    @Autowired
    ApplicationFormRepository applicationFormRepository;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    SemesterRepository semesterRepository;

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

    @Test
    void 전달_받은_ID_중_지원서를_제출하지_않은_지원자의_이메일_목록_조회() {
        // given
        Applicant be5 = createApplicant("이젝트", "01011112231", "be5@test.kr", BE, Role.APPLY);
        Applicant be6 = createApplicant("박젝트", "01011112232", "be6@test.kr", BE, Role.APPLY);
        Applicant pm7 = createApplicant("서젝트", "01011112233", "pm7@test.kr", PM, Role.APPLY);
        Applicant fe8 = createApplicant("양젝트", "01011112234", "fe8@test.kr", FE, Role.APPLY);
        Applicant be9 = createApplicant("조젝트", "01011112235", "be9@test.kr", BE, Role.APPLY);
        Applicant pd10 = createApplicant("표젝트", "01011112236", "pd10@test.kr", PD, Role.APPLY);
        List<Applicant> testApplicants = applicantRepository.saveAll(List.of(be5, be6, pm7, fe8, be9, pd10));

        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());
        Recruit pmRecruit = createRecruit(savedSemester, PM);
        Recruit pdRecruit = createRecruit(savedSemester, PD);
        Recruit feRecruit = createRecruit(savedSemester, FE);
        Recruit beRecruit = createRecruit(savedSemester, BE);
        recruitRepository.saveAll(List.of(pmRecruit, pdRecruit, feRecruit, beRecruit));

        // 일부 지원자만 지원서 제출
        Apply be5Apply = applyRepository.save(createApply(beRecruit, be5, SUBMITTED));
        applyRepository.save(createApply(beRecruit, be6, JOINED));
        Apply pm7Apply = applyRepository.save(createApply(pmRecruit, pm7, SUBMITTED));
        Apply fe8Apply = applyRepository.save(createApply(feRecruit, fe8, SUBMITTED));
        applyRepository.save(createApply(beRecruit, be9, JOINED));
        applyRepository.save(createApply(pdRecruit, pd10, TEMP_SAVED));

        applicationFormRepository.saveAll(List.of(
                createApplicationForm(be5Apply),
                createApplicationForm(pm7Apply),
                createApplicationForm(fe8Apply)));

        List<Long> applicantIds = testApplicants.stream().map(Applicant::getId).toList();

        // when
        List<String> result = applicantRepository.findEmailsByIdsAndNotSubmitted(applicantIds);

        // then
        assertThat(result).asList()
                .hasSize(3)
                .containsExactlyInAnyOrder(be6.getEmail(), be9.getEmail(), pd10.getEmail());
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

    private Recruit createRecruit(Semester semester, JobFamily jobFamily) {
        return Recruit.builder()
                .semester(semester)
                .jobFamily(jobFamily)
                .startDate(java.time.LocalDateTime.now().minusDays(1))
                .endDate(java.time.LocalDateTime.now().plusDays(1))
                .build();
    }

    private Apply createApply(Recruit recruit, Applicant applicant, ApplyStatus status) {
        return Apply.builder()
                .applicant(applicant)
                .recruit(recruit)
                .status(status)
                .build();
    }

    private ApplicationForm createApplicationForm(Apply apply) {
        return ApplicationForm.builder()
                .content("content")
                .apply(apply)
                .build();
    }
}
