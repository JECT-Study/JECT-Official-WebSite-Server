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
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void JobFamily로_제출된_지원서_목록_조회_성공() {
        // given
        Applicant beApplicant1 = createApplicant("be1@test.com", BE);
        Applicant beApplicant2 = createApplicant("be2@test.com", BE);
        Applicant feApplicant = createApplicant("fe@test.com", FE);
        applicantRepository.saveAll(List.of(beApplicant1, beApplicant2, feApplicant));

        Apply beApply1 = getApply(beApplicant1, beRecruit, SUBMITTED);
        Apply beApply2 = getApply(beApplicant2, beRecruit, SUBMITTED);
        Apply feApply = getApply(feApplicant, feRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(beApply1, beApply2, feApply));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, BE, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Apply::getApplicant)
                .containsExactlyInAnyOrder(beApplicant1, beApplicant2);
    }

    @Test
    void JobFamily가_null이면_전체_제출된_지원서_조회() {
        // given
        Applicant beApplicant = createApplicant("be@test.com", BE);
        Applicant feApplicant = createApplicant("fe@test.com", FE);
        Applicant pmApplicant = createApplicant("pm@test.com", PM);
        applicantRepository.saveAll(List.of(beApplicant, feApplicant, pmApplicant));

        Apply beApply = getApply(beApplicant, beRecruit, SUBMITTED);
        Apply feApply = getApply(feApplicant, feRecruit, SUBMITTED);
        Apply pmApply = getApply(pmApplicant, pmRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(beApply, feApply, pmApply));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, null, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void 임시저장_상태는_조회되지_않음() {
        // given
        Applicant beApplicant1 = createApplicant("be1@test.com", BE);
        Applicant beApplicant2 = createApplicant("be2@test.com", BE);
        applicantRepository.saveAll(List.of(beApplicant1, beApplicant2));

        Apply submittedApply = getApply(beApplicant1, beRecruit, SUBMITTED);
        Apply tempApply = getApply(beApplicant2, beRecruit, TEMP_SAVED);
        applyRepository.saveAll(List.of(submittedApply, tempApply));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, BE, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void 삭제된_회원의_지원서는_조회되지_않음() {
        // given
        Applicant activeApplicant = createApplicant("active@test.com", BE);
        Applicant deletedApplicant = createApplicant("deleted@test.com", BE);
        deletedApplicant.deleteProfile();
        applicantRepository.saveAll(List.of(activeApplicant, deletedApplicant));

        Apply activeApply = getApply(activeApplicant, beRecruit, SUBMITTED);
        Apply deletedApply = getApply(deletedApplicant, beRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(activeApply, deletedApply));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, BE, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getApplicant()).isEqualTo(activeApplicant);
    }

    @Test
    void 페이징_정상_작동() {
        // given
        for (int i = 1; i <= 25; i++) {
            Applicant applicant = createApplicant("be" + i + "@test.com", BE);
            applicantRepository.save(applicant);
            Apply apply = getApply(applicant, beRecruit, SUBMITTED);
            applyRepository.save(apply);
        }

        Pageable pageable = PageRequest.of(1, 10);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, BE, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
    }

    @Test
    void 제출된_지원서가_없으면_빈_페이지_반환() {
        // given
        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, BE, null), pageable);


        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
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
    void createdAt_기준_내림차순_정렬()  {
        // given
        Applicant applicant1 = createApplicant("be1@test.com", BE);
        Applicant applicant2 = createApplicant("be2@test.com", BE);
        Applicant applicant3 = createApplicant("be3@test.com", BE);
        applicantRepository.saveAll(List.of(applicant1, applicant2, applicant3));

        Apply apply1 = getApply(applicant1, beRecruit, SUBMITTED);
        applyRepository.save(apply1);

        Apply apply2 = getApply(applicant2, beRecruit, SUBMITTED);
        applyRepository.save(apply2);

        Apply apply3 = getApply(applicant3, beRecruit, SUBMITTED);
        applyRepository.save(apply3);

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, BE, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0)).isEqualTo(apply3);
        assertThat(result.getContent().get(1)).isEqualTo(apply2);
        assertThat(result.getContent().get(2)).isEqualTo(apply1);
    }

    @Test
    void semesterId로_제출된_지원서_필터링_조회() {
        // given
        Semester semester2 = semesterRepository.save(Semester.builder()
                .name("2기")
                .isRecruiting(false)
                .build());

        Recruit recruit1 = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .jobFamily(BE)
                .build());

        Recruit recruit2 = recruitRepository.save(Recruit.builder()
                .semester(semester2)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .jobFamily(BE)
                .build());

        Applicant applicant1 = createApplicant("be1@test.com", BE);
        Applicant applicant2 = createApplicant("be2@test.com", BE);
        applicantRepository.saveAll(List.of(applicant1, applicant2));

        Apply apply1 = getApply(applicant1, recruit1, SUBMITTED);
        Apply apply2 = getApply(applicant2, recruit2, SUBMITTED);
        applyRepository.saveAll(List.of(apply1, apply2));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, semester.getId(), BE, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getRecruit().getSemester()).isEqualTo(semester);
    }

    @Test
    void recruitType으로_제출된_지원서_필터링_조회() {
        // given
        Recruit regularRecruit = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .jobFamily(BE)
                .recruitType(RecruitType.REGULAR)
                .build());

        Recruit backfillRecruit = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .jobFamily(BE)
                .recruitType(RecruitType.BACKFILL)
                .build());

        Applicant applicant1 = createApplicant("regular@test.com", BE);
        Applicant applicant2 = createApplicant("backfill@test.com", BE);
        applicantRepository.saveAll(List.of(applicant1, applicant2));

        Apply apply1 = getApply(applicant1, regularRecruit, SUBMITTED);
        Apply apply2 = getApply(applicant2, backfillRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(apply1, apply2));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(condition(status, null, null, RecruitType.BACKFILL), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getRecruit().getRecruitType()).isEqualTo(RecruitType.BACKFILL);
    }

    @Test
    void recruitTypeDetail로_제출된_지원서_필터링_조회() {
        // given
        Recruit regularRecruit = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .jobFamily(BE)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REGULAR)
                .build());

        Recruit refillRecruit = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .jobFamily(BE)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .build());

        Applicant regularApplicant = createApplicant("regular-detail@test.com", BE);
        Applicant refillApplicant = createApplicant("refill-detail@test.com", BE);
        applicantRepository.saveAll(List.of(regularApplicant, refillApplicant));

        Apply regularApply = getApply(regularApplicant, regularRecruit, SUBMITTED);
        Apply refillApply = getApply(refillApplicant, refillRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(regularApply, refillApply));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(
                condition(status, null, null, null, RecruitTypeDetail.REFILL, null), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getRecruit().getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REFILL);
    }

    @Test
    void recruitId로_제출된_지원서_필터링_조회() {
        // given
        Applicant beApplicant = createApplicant("be-recruit-id@test.com", BE);
        Applicant feApplicant = createApplicant("fe-recruit-id@test.com", FE);
        applicantRepository.saveAll(List.of(beApplicant, feApplicant));

        Apply beApply = getApply(beApplicant, beRecruit, SUBMITTED);
        Apply feApply = getApply(feApplicant, feRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(beApply, feApply));

        Pageable pageable = PageRequest.of(0, 15);
        ApplyStatus status = SUBMITTED;

        // when
        Page<Apply> result = adminApplyRepository.findApplies(
                condition(status, null, null, null, null, beRecruit.getId()), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getRecruit()).isEqualTo(beRecruit);
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
        adminApplyRepository.deleteAllByIds(List.of(savedApply.getId()));
        entityManager.flush();

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
                    adminApplyRepository.deleteAllByIds(List.of(applyId)));

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

    private AdminApplySearchCondition condition(ApplyStatus status,
                                                Long semesterId,
                                                JobFamily jobFamily,
                                                RecruitType recruitType) {
        return condition(status, semesterId, jobFamily, recruitType, null, null);
    }

    private AdminApplySearchCondition condition(ApplyStatus status,
                                                Long semesterId,
                                                JobFamily jobFamily,
                                                RecruitType recruitType,
                                                RecruitTypeDetail recruitTypeDetail,
                                                Long recruitId) {
        return new AdminApplySearchCondition(status, semesterId, jobFamily, recruitType, recruitTypeDetail, recruitId);
    }
}
