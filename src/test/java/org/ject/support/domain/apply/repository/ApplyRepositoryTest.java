package org.ject.support.domain.apply.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

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
class ApplyRepositoryTest {

    @Autowired
    ApplyRepository applyRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    EntityManager entityManager;

    Semester semester;
    Recruit pmRecruit;
    Recruit pdRecruit;
    Recruit feRecruit;
    Recruit beRecruit;

    @Autowired
    private ApplicantRepository applicantRepository;

    @BeforeEach
    void setUp() {
        semester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());
        pmRecruit = getRecruit(PM);
        pdRecruit = getRecruit(PD);
        feRecruit = getRecruit(FE);
        beRecruit = getRecruit(BE);
        recruitRepository.saveAll(List.of(pmRecruit, pdRecruit, feRecruit, beRecruit));
    }

    @Test
    void 모집과_지원상태를_바탕으로_임시저장_상태의_지원정보_조회_성공() {
        // given
        Applicant feApplicant = createApplicant("emailFE@test.com", Role.APPLY);
        Applicant be1Applicant = createApplicant("emailBE1@test.com", Role.APPLY);
        Applicant be2Applicant = createApplicant("emailBE2@test.com", Role.APPLY);
        Applicant be3Applicant = createApplicant("emailBE3@test.com", Role.APPLY);
        applicantRepository.saveAll(List.of(feApplicant, be1Applicant, be2Applicant, be3Applicant));

        Apply feTempApply = getApply(feApplicant, feRecruit, TEMP_SAVED);
        Apply be1SubmitApply = getApply(be1Applicant, beRecruit, SUBMITTED);
        Apply be2TempApply = getApply(be2Applicant, beRecruit, TEMP_SAVED);
        Apply be3TempApply = getApply(be3Applicant, beRecruit, TEMP_SAVED);
        applyRepository.saveAll(List.of(feTempApply, be1SubmitApply, be2TempApply, be3TempApply));

        // when
        List<Apply> result = applyRepository.findByRecruitAndStatus(beRecruit, TEMP_SAVED);

        // then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(be2TempApply, be3TempApply);
    }

    @Test
    void 상태별_지원서_수_조회_성공() {
        // given
        Applicant feApplicant = createApplicant("emailFE@test.com", Role.APPLY);
        Applicant beApplicant = createApplicant("emailBE@test.com", Role.APPLY);
        applicantRepository.saveAll(List.of(feApplicant, beApplicant));

        Apply feTempApply = getApply(feApplicant, feRecruit, TEMP_SAVED);
        Apply beSubmitApply = getApply(beApplicant, beRecruit, SUBMITTED);
        applyRepository.saveAll(List.of(feTempApply, beSubmitApply));

        // when
        Long count = applyRepository.countByStatus(SUBMITTED);

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void 회원과_모집_공고를_바탕으로_활성_지원정보_존재_여부를_조회한다() {
        // given
        Applicant applicant = createApplicant("email@test.com", Role.APPLY);
        applicantRepository.save(applicant);

        Apply feApply = getApply(applicant, feRecruit, JOINED);
        Apply beApply = getApply(applicant, beRecruit, JOINED);
        applyRepository.saveAll(List.of(feApply, beApply));

        // when
        boolean exists = applyRepository.existsByApplicantIdAndRecruitIdInActiveRecruit(
                applicant.getId(), beRecruit.getId(), LocalDateTime.now());
        boolean notExists = applyRepository.existsByApplicantIdAndRecruitIdInActiveRecruit(
                applicant.getId(), pdRecruit.getId(), LocalDateTime.now());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void 회원과_모집_공고를_바탕으로_활성_지원정보를_조회한다() {
        // given
        Applicant applicant = createApplicant("email@test.com", Role.APPLY);
        applicantRepository.save(applicant);

        Apply feApply = getApply(applicant, feRecruit, JOINED);
        Apply beApply = getApply(applicant, beRecruit, TEMP_SAVED);
        applyRepository.saveAll(List.of(feApply, beApply));

        // when
        Apply result = applyRepository.findByApplicantIdAndRecruitIdInActiveRecruit(
                applicant.getId(), beRecruit.getId(), LocalDateTime.now()).orElseThrow();

        // then
        assertThat(result).isEqualTo(beApply);
        assertThat(result.getRecruit()).isEqualTo(beRecruit);
    }

    @Test
    void 지원자가_작성한_공고_ID를_조회한다() {
        // given
        Applicant applicant = createApplicant("email@test.com", Role.APPLY);
        applicantRepository.save(applicant);

        applyRepository.save(getApply(applicant, beRecruit, TEMP_SAVED));

        // when
        Long result = applyRepository.findRecruitIdByApplicantId(applicant.getId()).orElseThrow();

        // then
        assertThat(result).isEqualTo(beRecruit.getId());
    }

    @Test
    void 지원ID와_지원서의_상태로_지원서를_상세_조회한다() {
        // given
        Applicant feApplicant = createApplicant("emailFE@test.com", Role.APPLY);
        Applicant be1Applicant = createApplicant("emailBE1@test.com", Role.APPLY);
        applicantRepository.saveAll(List.of(feApplicant, be1Applicant));

        Apply feTempApply = getApply(feApplicant, feRecruit, TEMP_SAVED);
        Apply be1SubmitApply = getApply(be1Applicant, beRecruit, TEMP_SAVED);
        applyRepository.saveAll(List.of(feTempApply, be1SubmitApply));

        // when
        Apply result = applyRepository.findByIdAndStatusWithApplicant(feTempApply.getId(), TEMP_SAVED)
                .orElseThrow();

        // then
        assertThat(result).isEqualTo(feTempApply);
        assertThat(result.getApplicant()).isEqualTo(feApplicant);
        assertThat(result.getRecruit()).isEqualTo(feRecruit);
        assertThat(result.getStatus()).isEqualTo(TEMP_SAVED);
    }

    @Test
    void 지원ID_목록으로_회원과_모집을_함께_조회한다() {
        // given
        Applicant applicant = createApplicant("email@test.com", Role.APPLY);
        applicantRepository.save(applicant);
        Apply savedApply = applyRepository.save(getApply(applicant, beRecruit, SUBMITTED));
        entityManager.flush();
        entityManager.clear();

        // when
        Apply result = applyRepository.findAllByIdWithApplicant(List.of(savedApply.getId())).get(0);

        // then
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(result.getApplicant())).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(result.getRecruit())).isTrue();
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
        return Apply.builder()
                .recruit(recruit)
                .applicant(applicant)
                .status(status)
                .build();
    }

    private Applicant createApplicant(String email, Role role) {
        return Applicant.builder()
                .email(email)
                .semesterId(1L)
                .role(role)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
