package org.ject.support.admin.apply.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.apply.domain.ApplyStatus.TEMP_SAVED;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ject.support.admin.apply.dto.WaitlistOrderSearchCondition;
import org.ject.support.admin.apply.dto.SubmittedApplyProjection;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyProjection;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Import({QueryDslTestConfig.class, AdminApplyQueryRepositoryImpl.class})
@DataJpaTest
class AdminApplyQueryRepositoryTest {

    @Autowired
    AdminApplyRepository adminApplyRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    ApplicantRepository applicantRepository;

    @Autowired
    org.ject.support.domain.apply.repository.ApplyRepository applyRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    PlatformTransactionManager transactionManager;

    Semester semester;
    Recruit beRecruit;
    Recruit feRecruit;
    Recruit pmRecruit;

    @BeforeEach
    void setUp() {
        semester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        beRecruit = getRecruit(BE);
        feRecruit = getRecruit(FE);
        pmRecruit = getRecruit(PM);
        recruitRepository.saveAll(List.of(beRecruit, feRecruit, pmRecruit));
    }

    @Test
    @DisplayName("선택한 공고의 제출완료 지원서만 조회한다")
    void 선택한_공고의_제출완료_지원서만_조회한다() {
        // given
        Applicant submittedApplicant = saveApplicant("submitted@test.com", BE, CareerDetails.STUDENT);
        Applicant tempSavedApplicant = saveApplicant("temp@test.com", BE, CareerDetails.STUDENT);
        Applicant otherRecruitApplicant = saveApplicant("other@test.com", FE, CareerDetails.STUDENT);
        Apply submittedApply = applyRepository.save(getApply(submittedApplicant, beRecruit, SUBMITTED));
        applyRepository.save(getApply(tempSavedApplicant, beRecruit, TEMP_SAVED));
        applyRepository.save(getApply(otherRecruitApplicant, feRecruit, SUBMITTED));
        var condition = submittedCondition(beRecruit.getId(), null, null, null);

        // when
        List<SubmittedApplyProjection> result =
                adminApplyRepository.searchSubmittedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(SubmittedApplyProjection::applyId)
                .containsExactly(submittedApply.getId());
    }

    @Test
    @DisplayName("지원자 신분과 선정 결과 다중 필터를 함께 적용한다")
    void 지원자_신분과_선정_결과_다중_필터를_함께_적용한다() {
        // given
        Apply studentPassed = saveSubmittedApply(
                "student-passed@test.com", CareerDetails.STUDENT, SelectionResult.PASSED, null);
        Apply employeeWaitlisted = saveSubmittedApply(
                "employee-waitlisted@test.com", CareerDetails.EMPLOYEE, SelectionResult.WAITLISTED, 1);
        saveSubmittedApply(
                "between-jobs-passed@test.com", CareerDetails.BETWEEN_JOBS, SelectionResult.PASSED, null);
        saveSubmittedApply(
                "student-failed@test.com", CareerDetails.STUDENT, SelectionResult.FAILED, null);
        var condition = new SubmittedApplySearchCondition(
                beRecruit.getId(),
                Set.of(CareerDetails.STUDENT, CareerDetails.EMPLOYEE),
                Set.of(SelectionResult.PASSED, SelectionResult.WAITLISTED),
                null,
                20);

        // when
        List<SubmittedApplyProjection> result =
                adminApplyRepository.searchSubmittedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(SubmittedApplyProjection::applyId)
                .containsExactlyInAnyOrder(studentPassed.getId(), employeeWaitlisted.getId());
    }

    @Test
    @DisplayName("제출완료 지원서를 ID 내림차순으로 조회하고 커서 이전 지원서만 반환한다")
    void 제출완료_지원서를_ID_내림차순으로_조회하고_커서_이전_지원서만_반환한다() {
        // given
        Apply first = saveSubmittedApply(
                "cursor-first@test.com", CareerDetails.STUDENT, SelectionResult.UNDECIDED, null);
        Apply second = saveSubmittedApply(
                "cursor-second@test.com", CareerDetails.STUDENT, SelectionResult.UNDECIDED, null);
        Apply third = saveSubmittedApply(
                "cursor-third@test.com", CareerDetails.STUDENT, SelectionResult.UNDECIDED, null);
        var condition = submittedCondition(beRecruit.getId(), null, null, third.getId());

        // when
        List<SubmittedApplyProjection> result =
                adminApplyRepository.searchSubmittedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(SubmittedApplyProjection::applyId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("제출완료 지원서 전체 개수는 커서와 관계없이 필터 조건으로 계산한다")
    void 제출완료_지원서_전체_개수는_커서와_관계없이_필터_조건으로_계산한다() {
        // given
        saveSubmittedApply("count-first@test.com", CareerDetails.STUDENT, SelectionResult.PASSED, null);
        Apply second = saveSubmittedApply(
                "count-second@test.com", CareerDetails.STUDENT, SelectionResult.PASSED, null);
        saveSubmittedApply("count-third@test.com", CareerDetails.STUDENT, SelectionResult.PASSED, null);
        saveSubmittedApply("count-excluded@test.com", CareerDetails.EMPLOYEE, SelectionResult.PASSED, null);
        var condition = new SubmittedApplySearchCondition(
                beRecruit.getId(),
                Set.of(CareerDetails.STUDENT),
                Set.of(SelectionResult.PASSED),
                second.getId(),
                20);

        // when
        long result = adminApplyRepository.countSubmittedApplies(condition);

        // then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    @DisplayName("삭제된 지원서와 삭제된 지원자의 지원서는 제출완료 목록에서 제외한다")
    void 삭제된_지원서와_삭제된_지원자의_지원서는_제출완료_목록에서_제외한다() {
        // given
        Apply activeApply = saveSubmittedApply(
                "active-submitted@test.com", CareerDetails.STUDENT, SelectionResult.UNDECIDED, null);
        Apply deletedApply = saveSubmittedApply(
                "deleted-apply@test.com", CareerDetails.STUDENT, SelectionResult.UNDECIDED, null);
        Applicant deletedApplicant = saveApplicant(
                "deleted-user@test.com", BE, CareerDetails.STUDENT);
        Long deletedApplicantId = deletedApplicant.getId();
        applyRepository.save(getApply(deletedApplicant, beRecruit, SUBMITTED));
        applyRepository.flush();
        applyRepository.delete(deletedApply);
        applyRepository.flush();
        entityManager.clear();
        applicantRepository.deleteById(deletedApplicantId);
        applicantRepository.flush();
        entityManager.clear();
        var condition = submittedCondition(beRecruit.getId(), null, null, null);

        // when
        List<SubmittedApplyProjection> result =
                adminApplyRepository.searchSubmittedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(SubmittedApplyProjection::applyId)
                .containsExactly(activeApply.getId());
    }

    @Test
    @DisplayName("선택한 공고의 임시저장 지원서만 조회한다")
    void 선택한_공고의_임시저장_지원서만_조회한다() {
        // given
        Applicant tempSavedApplicant = saveApplicant("temp-only@test.com", BE, CareerDetails.STUDENT);
        Applicant submittedApplicant = saveApplicant("submitted-excluded@test.com", BE, CareerDetails.STUDENT);
        Applicant otherRecruitApplicant = saveApplicant("other-temp@test.com", FE, CareerDetails.STUDENT);
        Apply tempSavedApply = applyRepository.save(getApply(tempSavedApplicant, beRecruit, TEMP_SAVED));
        applyRepository.save(getApply(submittedApplicant, beRecruit, SUBMITTED));
        applyRepository.save(getApply(otherRecruitApplicant, feRecruit, TEMP_SAVED));
        var condition = tempSavedCondition(beRecruit.getId(), null, null);

        // when
        List<TempSavedApplyProjection> result =
                adminApplyRepository.searchTempSavedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(TempSavedApplyProjection::applyId)
                .containsExactly(tempSavedApply.getId());
    }

    @Test
    @DisplayName("지원자 신분을 여러 개 선택하면 해당하는 임시저장 지원서를 조회한다")
    void 지원자_신분을_여러개_선택하면_해당하는_임시저장_지원서를_조회한다() {
        // given
        Apply student = saveTempSavedApply("temp-student@test.com", CareerDetails.STUDENT);
        Apply employee = saveTempSavedApply("temp-employee@test.com", CareerDetails.EMPLOYEE);
        saveTempSavedApply("temp-excluded@test.com", CareerDetails.JOB_SEEKER);
        var condition = new TempSavedApplySearchCondition(
                beRecruit.getId(),
                Set.of(CareerDetails.STUDENT, CareerDetails.EMPLOYEE),
                null,
                20);

        // when
        List<TempSavedApplyProjection> result =
                adminApplyRepository.searchTempSavedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(TempSavedApplyProjection::applyId)
                .containsExactlyInAnyOrder(student.getId(), employee.getId());
    }

    @Test
    @DisplayName("임시저장 지원서를 ID 내림차순으로 조회하고 커서 이전 지원서만 반환한다")
    void 임시저장_지원서를_ID_내림차순으로_조회하고_커서_이전_지원서만_반환한다() {
        // given
        Apply first = saveTempSavedApply("temp-cursor-first@test.com", CareerDetails.STUDENT);
        Apply second = saveTempSavedApply("temp-cursor-second@test.com", CareerDetails.STUDENT);
        Apply third = saveTempSavedApply("temp-cursor-third@test.com", CareerDetails.STUDENT);
        var condition = tempSavedCondition(beRecruit.getId(), null, third.getId());

        // when
        List<TempSavedApplyProjection> result =
                adminApplyRepository.searchTempSavedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(TempSavedApplyProjection::applyId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("임시저장 지원서 전체 개수는 커서와 관계없이 신분 필터로 계산한다")
    void 임시저장_지원서_전체_개수는_커서와_관계없이_신분_필터로_계산한다() {
        // given
        saveTempSavedApply("temp-count-first@test.com", CareerDetails.STUDENT);
        Apply second = saveTempSavedApply("temp-count-second@test.com", CareerDetails.STUDENT);
        saveTempSavedApply("temp-count-third@test.com", CareerDetails.STUDENT);
        saveTempSavedApply("temp-count-excluded@test.com", CareerDetails.EMPLOYEE);
        var condition = new TempSavedApplySearchCondition(
                beRecruit.getId(),
                Set.of(CareerDetails.STUDENT),
                second.getId(),
                20);

        // when
        long result = adminApplyRepository.countTempSavedApplies(condition);

        // then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    @DisplayName("삭제된 지원서와 삭제된 지원자의 지원서는 임시저장 목록에서 제외한다")
    void 삭제된_지원서와_삭제된_지원자의_지원서는_임시저장_목록에서_제외한다() {
        // given
        Apply activeApply = saveTempSavedApply("active-temp@test.com", CareerDetails.STUDENT);
        Apply deletedApply = saveTempSavedApply("deleted-temp-apply@test.com", CareerDetails.STUDENT);
        Applicant deletedApplicant = saveApplicant(
                "deleted-temp@test.com", BE, CareerDetails.STUDENT);
        Long deletedApplicantId = deletedApplicant.getId();
        applyRepository.save(getApply(deletedApplicant, beRecruit, TEMP_SAVED));
        applyRepository.flush();
        applyRepository.delete(deletedApply);
        applyRepository.flush();
        entityManager.clear();
        applicantRepository.deleteById(deletedApplicantId);
        applicantRepository.flush();
        entityManager.clear();
        var condition = tempSavedCondition(beRecruit.getId(), null, null);

        // when
        List<TempSavedApplyProjection> result =
                adminApplyRepository.searchTempSavedApplies(condition, 20);

        // then
        assertThat(result)
                .extracting(TempSavedApplyProjection::applyId)
                .containsExactly(activeApply.getId());
    }

    @Test
    void ApplicationForm이_없는_지원서_상세조회_성공() {
        // given
        Applicant applicant = createApplicant("without-form@test.com", BE);
        applicantRepository.save(applicant);
        Apply apply = Apply.builder()
                .applicant(applicant)
                .recruit(beRecruit)
                .status(TEMP_SAVED)
                .build();
        applyRepository.saveAndFlush(apply);

        // when
        Optional<Apply> result = adminApplyRepository.findApplyByIdByStatus(apply.getId(), TEMP_SAVED);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getApplicationForm()).isNull();
    }

    @Test
    void 모집_공고와_지원ID로_지원서를_조회한다() {
        // given
        Applicant applicant = createApplicant("selection@test.com", BE);
        applicantRepository.save(applicant);
        Apply savedApply = applyRepository.save(getApply(applicant, beRecruit, SUBMITTED));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Apply> result = adminApplyRepository.findAllByRecruitIdAndIdInWithApplicant(
                beRecruit.getId(), List.of(savedApply.getId()));

        // then
        assertThat(result).singleElement()
                .satisfies(apply -> {
                    assertThat(apply.getId()).isEqualTo(savedApply.getId());
                    assertThat(apply.getApplicant().getId()).isEqualTo(applicant.getId());
                    assertThat(apply.getRecruit().getId()).isEqualTo(beRecruit.getId());
                });
    }

    @Test
    void 같은_모집_공고에서_예비_번호_중복을_DB가_막는다() {
        // given
        Applicant applicant1 = createApplicant("duplicate-selection-1@test.com", BE);
        Applicant applicant2 = createApplicant("duplicate-selection-2@test.com", BE);
        applicantRepository.saveAll(List.of(applicant1, applicant2));

        Apply apply1 = getApply(applicant1, beRecruit, SUBMITTED);
        Apply apply2 = getApply(applicant2, beRecruit, SUBMITTED);
        apply1.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        apply2.decideSelectionResult(SelectionResult.WAITLISTED, 1);

        // when, then
        assertThatThrownBy(() -> {
            applyRepository.saveAll(List.of(apply1, apply2));
            entityManager.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("다건 삭제하면 선정 결과를 미정으로 바꾸고 예비 번호를 비운다")
    void 다건_삭제하면_선정_결과와_예비_번호를_초기화한다() {
        // given
        Applicant applicant = createApplicant("bulk-delete@test.com", BE);
        applicantRepository.save(applicant);
        Apply apply = getApply(applicant, beRecruit, SUBMITTED);
        apply.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        Apply savedApply = applyRepository.save(apply);
        entityManager.flush();
        long versionBeforeDelete = ((Number) entityManager.createNativeQuery(
                        "SELECT version FROM apply WHERE id = :id")
                .setParameter("id", savedApply.getId())
                .getSingleResult()).longValue();

        // when
        adminApplyRepository.deleteAll(List.of(savedApply));
        entityManager.flush();
        entityManager.clear();

        // then
        Object[] state = (Object[]) entityManager.createNativeQuery(
                        "SELECT selection_result, waitlist_number, version FROM apply WHERE id = :id")
                .setParameter("id", savedApply.getId())
                .getSingleResult();
        assertThat(state[0]).isEqualTo("UNDECIDED");
        assertThat(state[1]).isNull();
        assertThat(((Number) state[2]).longValue()).isEqualTo(versionBeforeDelete + 1);
        assertThat(adminApplyRepository.findById(savedApply.getId())).isEmpty();
    }

    @Test
    @DisplayName("다건 삭제와 동시에 수정하면 낙관적 락 예외가 발생한다")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void 다건_삭제와_동시에_수정하면_낙관적_락_예외가_발생한다() {
        // given
        Applicant applicant = createApplicant("bulk-delete-lock@test.com", BE);
        applicantRepository.save(applicant);
        Apply savedApply = applyRepository.save(getApply(applicant, beRecruit, SUBMITTED));
        entityManager.flush();
        Long applyId = savedApply.getId();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        EntityManager staleEntityManager = entityManagerFactory.createEntityManager();
        try {
            staleEntityManager.getTransaction().begin();
            Apply staleApply = staleEntityManager.find(Apply.class, applyId);

            new TransactionTemplate(transactionManager).executeWithoutResult(transactionStatus ->
                    adminApplyRepository.deleteAll(
                            adminApplyRepository.findAllByIdWithApplicant(List.of(applyId))));

            // when, then
            staleApply.saveTemporarily();
            assertThatThrownBy(staleEntityManager::flush)
                    .isInstanceOf(OptimisticLockException.class);
        } finally {
            if (staleEntityManager.getTransaction().isActive()) {
                staleEntityManager.getTransaction().rollback();
            }
            staleEntityManager.close();
            TestTransaction.start();
        }
    }

    @Test
    void 단건_삭제하면_선정_결과와_예비_번호를_초기화한다() {
        // given
        Applicant applicant = createApplicant("single-delete@test.com", BE);
        applicantRepository.save(applicant);
        Apply apply = getApply(applicant, beRecruit, SUBMITTED);
        apply.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        Apply savedApply = applyRepository.save(apply);
        entityManager.flush();

        // when
        adminApplyRepository.delete(savedApply);
        entityManager.flush();

        // then
        Object[] state = (Object[]) entityManager.createNativeQuery(
                        "SELECT selection_result, waitlist_number FROM apply WHERE id = :id")
                .setParameter("id", savedApply.getId())
                .getSingleResult();
        assertThat(state[0]).isEqualTo("UNDECIDED");
        assertThat(state[1]).isNull();
        assertThat(adminApplyRepository.findById(savedApply.getId())).isEmpty();
    }

    @Test
    void 예비번호_순서로_다음_목록을_조회하고_전체_인원은_유지한다() {
        // given
        Apply first = saveSubmittedApply("order1@test.com", CareerDetails.STUDENT, SelectionResult.WAITLISTED, 3);
        Apply second = saveSubmittedApply("order2@test.com", CareerDetails.STUDENT, SelectionResult.WAITLISTED, 1);
        Apply third = saveSubmittedApply("order3@test.com", CareerDetails.STUDENT, SelectionResult.WAITLISTED, 2);
        saveSubmittedApply("passed@test.com", CareerDetails.STUDENT, SelectionResult.PASSED, null);
        saveTempSavedApply("draft@test.com", CareerDetails.STUDENT);
        Apply other = getApply(saveApplicant("foreign@test.com", FE, CareerDetails.STUDENT), feRecruit, SUBMITTED);
        other.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        applyRepository.saveAndFlush(other);

        // when
        var firstPage = adminApplyRepository.searchWaitlistOrders(
                new WaitlistOrderSearchCondition(beRecruit.getId(), null, 2), 2);
        var nextPage = adminApplyRepository.searchWaitlistOrders(
                new WaitlistOrderSearchCondition(beRecruit.getId(), 2L, 2), 2);

        // then
        assertThat(firstPage).extracting(r -> r.applyId()).containsExactly(second.getId(), third.getId());
        assertThat(nextPage).extracting(r -> r.applyId()).containsExactly(first.getId());
        assertThat(firstPage.getFirst().name()).isEqualTo(second.getApplicant().getName());
        assertThat(firstPage.getFirst().phoneNumber()).isEqualTo(second.getApplicant().getPhoneNumber());
        assertThat(firstPage.getFirst().careerDetails()).isEqualTo(CareerDetails.STUDENT);
        assertThat(adminApplyRepository.countWaitlistOrders(beRecruit.getId())).isEqualTo(3);
    }

    @Test
    void 삭제된_지원서와_지원자는_예비목록과_전체_인원에서_제외한다() {
        // given
        Apply visible = saveSubmittedApply("visible@test.com", CareerDetails.STUDENT, SelectionResult.WAITLISTED, 1);
        Apply deleted = saveSubmittedApply("deleted@test.com", CareerDetails.STUDENT, SelectionResult.WAITLISTED, 2);
        Apply deletedApplicant = saveSubmittedApply("deletedperson@test.com", CareerDetails.STUDENT, SelectionResult.WAITLISTED, 3);
        Long deletedId = deleted.getId();
        Long deletedApplicantId = deletedApplicant.getId();
        Long applicantId = deletedApplicant.getApplicant().getId();
        applyRepository.delete(deleted);
        entityManager.flush();
        entityManager.clear();
        applicantRepository.deleteById(applicantId);
        entityManager.flush();
        entityManager.clear();

        // when
        var content = adminApplyRepository.searchWaitlistOrders(
                new WaitlistOrderSearchCondition(beRecruit.getId(), null, 20), 20);

        // then
        assertThat(content).extracting(r -> r.applyId()).containsExactly(visible.getId());
        assertThat(adminApplyRepository.countWaitlistOrders(beRecruit.getId())).isEqualTo(1);
        assertThat(adminApplyRepository.findWaitlistedApplies(beRecruit.getId()))
                .extracting(Apply::getId).containsExactly(visible.getId());
        assertThat(adminApplyRepository.findAppliesInRecruitWithApplicant(
                beRecruit.getId(), List.of(visible.getId(), deletedId, deletedApplicantId)))
                .extracting(Apply::getId).containsExactly(visible.getId());
        assertThat(adminApplyRepository.findAppliesWithApplicant(List.of(deletedId, deletedApplicantId))).isEmpty();
    }

    private Recruit getRecruit(JobFamily jobFamily) {
        return Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .build();
    }

    private Apply getApply(Applicant applicant, Recruit recruit, ApplyStatus status) {
        ApplicationForm applicationForm = ApplicationForm.builder()
                .build();

        Apply apply = Apply.builder()
                .recruit(recruit)
                .applicant(applicant)
                .status(status)
                .applicationForm(applicationForm)
                .build();

        setField(applicationForm, "apply", apply);

        return apply;
    }

    private Applicant createApplicant(String email, JobFamily jobFamily) {
        return Applicant.builder()
                .email(email)
                .jobFamily(jobFamily)
                .semesterId(1L)
                .role(Role.APPLY)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }


    private Applicant saveApplicant(String email, JobFamily jobFamily, CareerDetails careerDetails) {
        Applicant applicant = Applicant.builder()
                .email(email)
                .name("지원자")
                .phoneNumber("01012345678")
                .jobFamily(jobFamily)
                .careerDetails(careerDetails)
                .semesterId(1L)
                .role(Role.APPLY)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
        return applicantRepository.save(applicant);
    }

    private Apply saveSubmittedApply(
            String email,
            CareerDetails careerDetails,
            SelectionResult selectionResult,
            Integer waitlistNumber
    ) {
        Applicant applicant = saveApplicant(email, BE, careerDetails);
        Apply apply = getApply(applicant, beRecruit, SUBMITTED);
        apply.decideSelectionResult(selectionResult, waitlistNumber);
        return applyRepository.save(apply);
    }

    private Apply saveTempSavedApply(String email, CareerDetails careerDetails) {
        Applicant applicant = saveApplicant(email, BE, careerDetails);
        return applyRepository.save(getApply(applicant, beRecruit, TEMP_SAVED));
    }

    private SubmittedApplySearchCondition submittedCondition(
            Long recruitId,
            Set<CareerDetails> careerDetails,
            Set<SelectionResult> selectionResults,
            Long cursor
    ) {
        return new SubmittedApplySearchCondition(
                recruitId, careerDetails, selectionResults, cursor, 20);
    }

    private TempSavedApplySearchCondition tempSavedCondition(
            Long recruitId,
            Set<CareerDetails> careerDetails,
            Long cursor
    ) {
        return new TempSavedApplySearchCondition(recruitId, careerDetails, cursor, 20);
    }
}
