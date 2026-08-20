package org.ject.support.admin.mail.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.member.JobFamily.BE;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
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

@Import({QueryDslTestConfig.class, MailTargetQueryRepository.class})
@DataJpaTest
class MailTargetQueryRepositoryTest {

    @Autowired
    private MailTargetQueryRepository mailTargetQueryRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private EntityManager entityManager;

    private Recruit recruit;

    @BeforeEach
    void setUp() {
        Semester semester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());
        recruit = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build());
    }

    @Test
    void 제출_완료되고_지원서가_있는_대상만_제출_시각과_ID_내림차순으로_조회한다() {
        // given
        LocalDateTime latest = LocalDateTime.of(2025, 1, 3, 10, 0);
        LocalDateTime sameSubmittedAt = LocalDateTime.of(2025, 1, 2, 10, 0);
        Apply latestApply = saveTarget("latest@test.com", latest, SelectionResult.PASSED, 99);
        Apply firstTieApply = saveTarget("first-tie@test.com", sameSubmittedAt, SelectionResult.FAILED);
        Apply secondTieApply = saveTarget("second-tie@test.com", sameSubmittedAt, SelectionResult.WAITLISTED, 2);
        saveApplyWithoutForm("without-form@test.com", SUBMITTED);
        saveApplyWithoutForm("temp@test.com", ApplyStatus.TEMP_SAVED);
        Apply deletedApply = saveTarget("deleted@test.com", latest, SelectionResult.PASSED);
        applyRepository.delete(deletedApply);
        entityManager.flush();
        entityManager.clear();

        // when
        List<MailTargetResponse> result = mailTargetQueryRepository.findTargets(recruit.getId(), null);

        // then
        assertThat(result)
                .extracting(MailTargetResponse::applyId)
                .containsExactly(latestApply.getId(), secondTieApply.getId(), firstTieApply.getId());
        assertThat(result.getFirst().waitlistNumber()).isNull();
    }

    @Test
    void 선정_결과로_메일_발송_대상을_필터링한다() {
        // given
        saveTarget("passed@test.com", LocalDateTime.now(), SelectionResult.PASSED);
        Apply waitlistedApply = saveTarget("waitlisted@test.com", LocalDateTime.now(), SelectionResult.WAITLISTED, 1);
        saveTarget("failed@test.com", LocalDateTime.now(), SelectionResult.FAILED);

        // when
        List<MailTargetResponse> result = mailTargetQueryRepository.findTargets(
                recruit.getId(), SelectionResult.WAITLISTED);

        // then
        assertThat(result).singleElement().satisfies(target -> {
            assertThat(target.applyId()).isEqualTo(waitlistedApply.getId());
            assertThat(target.email()).isEqualTo("waitlisted@test.com");
            assertThat(target.selectionResult()).isEqualTo(SelectionResult.WAITLISTED);
            assertThat(target.waitlistNumber()).isEqualTo(1);
        });
    }

    private Apply saveTarget(String email, LocalDateTime submittedAt, SelectionResult selectionResult) {
        return saveTarget(email, submittedAt, selectionResult, null);
    }

    private Apply saveTarget(String email,
                             LocalDateTime submittedAt,
                             SelectionResult selectionResult,
                             Integer waitlistNumber) {
        Applicant applicant = applicantRepository.save(Applicant.builder()
                .email(email)
                .name("테스트 사용자")
                .phoneNumber("01012345678")
                .jobFamily(BE)
                .semesterId(1L)
                .role(Role.APPLY)
                .status(MemberStatus.ACTIVE)
                .pin("123456")
                .build());
        Apply apply = Apply.builder()
                .applicant(applicant)
                .recruit(recruit)
                .status(SUBMITTED)
                .selectionResult(selectionResult)
                .waitlistNumber(waitlistNumber)
                .submittedAt(submittedAt)
                .build();
        apply.updateApplicationForm(ApplicationForm.builder().apply(apply).build());
        return applyRepository.save(apply);
    }

    private Apply saveApplyWithoutForm(String email, ApplyStatus status) {
        Applicant applicant = applicantRepository.save(Applicant.builder()
                .email(email)
                .name("테스트 사용자")
                .phoneNumber("01012345678")
                .jobFamily(BE)
                .semesterId(1L)
                .role(Role.APPLY)
                .status(MemberStatus.ACTIVE)
                .pin("123456")
                .build());
        return applyRepository.save(Apply.builder()
                .applicant(applicant)
                .recruit(recruit)
                .status(status)
                .build());
    }
}
