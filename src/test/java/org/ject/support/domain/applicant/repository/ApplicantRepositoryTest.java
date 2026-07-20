package org.ject.support.domain.applicant.repository;

import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.applicant.dto.ApplicantAccountProjection;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void 필터가_없으면_삭제되지_않은_백오피스_계정만_조회한다() {
        // given
        Applicant admin = createApplicant("관리자", "01011112221", "admin@ject.kr", BE, Role.ADMIN);
        Applicant operations = createApplicant("운영자", "01011112222", "operations@ject.kr", BE, Role.OPERATIONS);
        Applicant semester = createApplicant("일반회원", "01011112223", "semester@ject.kr", BE, Role.SEMESTER);
        Applicant deleted = createApplicant("삭제회원", "01011112224", "deleted@ject.kr", BE, Role.SUPPORTER);
        ReflectionTestUtils.setField(deleted, "isDeleted", true);
        applicantRepository.saveAll(List.of(admin, operations, semester, deleted));

        // when
        Page<ApplicantAccountProjection> result = applicantRepository.findAccounts(
                new AdminAccountSearchCondition(null, null),
                PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(ApplicantAccountProjection::email)
                .containsExactlyInAnyOrder("admin@ject.kr", "operations@ject.kr");
    }

    @Test
    void 계정_유형과_상태를_복수_필터로_조회한다() {
        // given
        Applicant activeAdmin = createApplicant("활성관리자", "01011112225", "active-admin@ject.kr", BE, Role.ADMIN);
        Applicant lockedAdmin = createApplicant("잠긴관리자", "01011112226", "locked-admin@ject.kr", BE, Role.ADMIN);
        Applicant activeSupporter = createApplicant("활성서포터", "01011112227", "active-supporter@ject.kr", BE, Role.SUPPORTER);
        Applicant lockedOperations = createApplicant("잠긴운영자", "01011112228", "locked-operations@ject.kr", BE, Role.OPERATIONS);
        ReflectionTestUtils.setField(lockedAdmin, "status", MemberStatus.LOCKED);
        ReflectionTestUtils.setField(lockedOperations, "status", MemberStatus.LOCKED);
        applicantRepository.saveAll(List.of(activeAdmin, lockedAdmin, activeSupporter, lockedOperations));

        AdminAccountSearchCondition condition = new AdminAccountSearchCondition(
                List.of(Role.ADMIN, Role.SUPPORTER),
                List.of(MemberStatus.ACTIVE));

        // when
        Page<ApplicantAccountProjection> result = applicantRepository.findAccounts(condition, PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(ApplicantAccountProjection::email)
                .containsExactlyInAnyOrder("active-admin@ject.kr", "active-supporter@ject.kr");
    }

    @Test
    void 페이지네이션과_기본_정렬을_적용한다() {
        // given
        Applicant first = createApplicant("첫번째", "01011112229", "first@ject.kr", BE, Role.ADMIN);
        Applicant second = createApplicant("두번째", "01011112230", "second@ject.kr", BE, Role.ADMIN);
        Applicant third = createApplicant("세번째", "01011112234", "third@ject.kr", BE, Role.ADMIN);
        setCreatedAt(first, LocalDateTime.of(2026, 1, 1, 0, 0));
        setCreatedAt(second, LocalDateTime.of(2026, 1, 2, 0, 0));
        setCreatedAt(third, LocalDateTime.of(2026, 1, 3, 0, 0));
        applicantRepository.saveAll(List.of(first, second, third));
        applicantRepository.flush();

        // when
        Page<ApplicantAccountProjection> result = applicantRepository.findAccounts(
                new AdminAccountSearchCondition(null, null),
                PageRequest.of(0, 2));

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(ApplicantAccountProjection::email)
                .containsExactly("third@ject.kr", "second@ject.kr");
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

    private void setCreatedAt(Applicant applicant, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(applicant, "createdAt", createdAt);
    }
}
