package org.ject.support.domain.recruit.repository;

import jakarta.persistence.EntityManager;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitFaq;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitProjection;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitSearchCondition;
import org.ject.support.domain.recruit.dto.RecruitTypeCountResponse;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.ject.support.domain.member.JobFamily.SUPPORTER;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class RecruitRepositoryTest {

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("지원서가 없는 공고도 지원서 관리 목록에 포함한다")
    void 지원서가_없는_공고도_지원서_관리_목록에_포함한다() {
        // given
        Semester semester = saveSemester("지원서 없는 공고 기수");
        Recruit recruit = saveRecruit(semester, FE, RecruitType.SEMESTER);
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                RecruitType.SEMESTER, Set.of(FE), null);

        // when
        List<ApplyManagementRecruitProjection> result =
                recruitRepository.searchApplyManagementRecruits(condition, 20);

        // then
        assertThat(result).singleElement().satisfies(found -> {
            assertThat(found.recruitId()).isEqualTo(recruit.getId());
            assertThat(found.submittedApplyCount()).isZero();
            assertThat(found.passedCount()).isZero();
            assertThat(found.failedCount()).isZero();
            assertThat(found.waitlistedCount()).isZero();
        });
    }

    @Test
    @DisplayName("모집 유형과 직군에 해당하는 공고만 조회한다")
    void 모집_유형과_직군에_해당하는_공고만_조회한다() {
        // given
        Semester semester = saveSemester("공고 필터 기수");
        Recruit matched = saveRecruit(semester, FE, RecruitType.SEMESTER);
        saveRecruit(semester, BE, RecruitType.SEMESTER);
        saveRecruit(semester, FE, RecruitType.MAKERS);
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                RecruitType.SEMESTER, Set.of(FE), null);

        // when
        List<ApplyManagementRecruitProjection> result =
                recruitRepository.searchApplyManagementRecruits(condition, 20);

        // then
        assertThat(result)
                .extracting(ApplyManagementRecruitProjection::recruitId)
                .containsExactly(matched.getId());
    }

    @Test
    @DisplayName("공고를 ID 내림차순으로 조회하고 커서 이전 공고만 반환한다")
    void 공고를_ID_내림차순으로_조회하고_커서_이전_공고만_반환한다() {
        // given
        Semester semester = saveSemester("공고 커서 기수");
        Recruit first = saveRecruit(semester, FE, RecruitType.SEMESTER);
        Recruit second = saveRecruit(semester, FE, RecruitType.SEMESTER);
        Recruit third = saveRecruit(semester, FE, RecruitType.SEMESTER);
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                RecruitType.SEMESTER, Set.of(FE), third.getId());

        // when
        List<ApplyManagementRecruitProjection> result =
                recruitRepository.searchApplyManagementRecruits(condition, 20);

        // then
        assertThat(result)
                .extracting(ApplyManagementRecruitProjection::recruitId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("제출완료되고 삭제되지 않은 지원서만 선정결과별 통계에 포함한다")
    void 제출완료되고_삭제되지_않은_지원서만_선정결과별_통계에_포함한다() {
        // given
        Semester semester = saveSemester("지원서 통계 기수");
        Recruit recruit = saveRecruit(semester, FE, RecruitType.SEMESTER);
        saveApply(recruit, ApplyStatus.SUBMITTED, SelectionResult.PASSED, "passed@test.com");
        saveApply(recruit, ApplyStatus.SUBMITTED, SelectionResult.FAILED, "failed@test.com");
        saveApply(recruit, ApplyStatus.SUBMITTED, SelectionResult.WAITLISTED, "waitlisted@test.com");
        saveApply(recruit, ApplyStatus.SUBMITTED, SelectionResult.UNDECIDED, "undecided@test.com");
        saveApply(recruit, ApplyStatus.TEMP_SAVED, SelectionResult.UNDECIDED, "temporary@test.com");
        Apply deleted = saveApply(
                recruit, ApplyStatus.SUBMITTED, SelectionResult.PASSED, "deleted@test.com");
        applyRepository.flush();
        applyRepository.delete(deleted);
        applyRepository.flush();
        entityManager.clear();

        ApplyManagementRecruitSearchCondition condition = searchCondition(
                RecruitType.SEMESTER, Set.of(FE), null);

        // when
        ApplyManagementRecruitProjection result = recruitRepository
                .searchApplyManagementRecruits(condition, 20)
                .getFirst();

        // then
        assertThat(result.submittedApplyCount()).isEqualTo(4L);
        assertThat(result.passedCount()).isEqualTo(1L);
        assertThat(result.failedCount()).isEqualTo(1L);
        assertThat(result.waitlistedCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("모집 유형별 전체 공고 수를 조회한다")
    void 모집_유형별_전체_공고_수를_조회한다() {
        // given
        Semester semester = saveSemester("유형별 공고 수 기수");
        saveRecruit(semester, FE, RecruitType.SEMESTER);
        saveRecruit(semester, BE, RecruitType.SEMESTER);
        saveRecruit(semester, FE, RecruitType.MAKERS);

        // when
        List<RecruitTypeCountResponse> result = recruitRepository.countRecruitsByRecruitType();

        // then
        assertThat(result)
                .containsExactlyInAnyOrder(
                        new RecruitTypeCountResponse(RecruitType.SEMESTER, 2L),
                        new RecruitTypeCountResponse(RecruitType.MAKERS, 1L));
    }

    @Test
    @DisplayName("공고 전체 개수는 커서와 관계없이 필터 조건으로 계산한다")
    void 공고_전체_개수는_커서와_관계없이_필터_조건으로_계산한다() {
        // given
        Semester semester = saveSemester("공고 전체 개수 기수");
        saveRecruit(semester, FE, RecruitType.SEMESTER);
        Recruit second = saveRecruit(semester, FE, RecruitType.SEMESTER);
        saveRecruit(semester, FE, RecruitType.SEMESTER);
        saveRecruit(semester, BE, RecruitType.SEMESTER);
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                RecruitType.SEMESTER, Set.of(FE), second.getId());

        // when
        long result = recruitRepository.countApplyManagementRecruits(condition);

        // then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void 모집_공고에_연결된_기수를_조회한다() {
        // given
        Semester savedSemester = semesterRepository.save(
                Semester.builder().name("5기").isRecruiting(true).build());
        Recruit savedRecruit = recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build());

        // when
        Long semesterId = semesterRepository.findSemesterIdByRecruitId(savedRecruit.getId())
                .orElseThrow();

        // then
        assertThat(semesterId).isEqualTo(savedSemester.getId());
    }

    @Test
    void 모집공고의_상세정보와_안내사항_및_FAQ를_저장하고_조회한다() {
        // given
        Semester semester = semesterRepository.save(Semester.builder().name("5기").isRecruiting(true).build());
        Recruit savedRecruit = recruitRepository.saveAndFlush(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(FE)
                .recruitInformation("<h2>모집 정보</h2>")
                .notice("<h2>안내사항</h2>")
                .faqs(List.of(RecruitFaq.create("지원 자격이 있나요?", "<p>누구나 지원할 수 있습니다.</p>")))
                .build());
        entityManager.clear();

        // when
        Recruit result = recruitRepository.findByIdWithSemester(savedRecruit.getId()).orElseThrow();

        // then
        assertThat(result.getRecruitInformation()).isEqualTo("<h2>모집 정보</h2>");
        assertThat(result.getNotice()).isEqualTo("<h2>안내사항</h2>");
        assertThat(result.getFaqs()).containsExactly(RecruitFaq.create("지원 자격이 있나요?", "<p>누구나 지원할 수 있습니다.</p>"));
    }

    @Test
    void 특정_직군의_마감되지_않은_모집_정보가_존재하면_true를_반환한다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());

        recruitRepository.saveAll(
                List.of(
                        Recruit.builder()
                                .semester(savedSemester)
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(1))
                                .jobFamily(PM)
                                .build(),
                        Recruit.builder()
                                .semester(savedSemester)
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(1))
                                .jobFamily(BE)
                                .build()
                )
        );

        // when
        boolean result = recruitRepository.existsByJobFamilyAndIsNotClosed(savedSemester.getId(), List.of(FE, BE));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 활성_모집_공고만_시작일_순서로_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());
        Recruit earlierActiveRecruit = recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.minusDays(2))
                .endDate(now.plusDays(2))
                .jobFamily(BE)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REGULAR)
                .build());
        Recruit laterActiveRecruit = recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(2))
                .jobFamily(SUPPORTER)
                .recruitType(RecruitType.SUPPORTERS)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .build());
        recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.minusDays(4))
                .endDate(now.minusDays(1))
                .jobFamily(FE)
                .recruitType(RecruitType.MAKERS)
                .recruitTypeDetail(RecruitTypeDetail.NEW)
                .build());
        recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(2))
                .jobFamily(PM)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .build());

        // when
        List<Recruit> result = recruitRepository.findActiveRecruitments(now);

        // then
        assertThat(result)
                .extracting(Recruit::getId)
                .containsExactly(earlierActiveRecruit.getId(), laterActiveRecruit.getId());
        assertThat(result.get(0).getSemester().getName()).isEqualTo("3기");
    }

    @Test
    void 메일_발송용_모집_공고를_등록일_내림차순으로_조회하고_동일하면_ID_내림차순으로_조회한다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());
        Recruit oldestRecruit = saveRecruit(savedSemester, PM);
        Recruit firstTieRecruit = saveRecruit(savedSemester, BE);
        Recruit secondTieRecruit = saveRecruit(savedSemester, FE);
        Recruit latestRecruit = saveRecruit(savedSemester, SUPPORTER);
        entityManager.flush();
        setCreatedAt(oldestRecruit, LocalDateTime.of(2026, 8, 1, 10, 0));
        setCreatedAt(firstTieRecruit, LocalDateTime.of(2026, 8, 2, 10, 0));
        setCreatedAt(secondTieRecruit, LocalDateTime.of(2026, 8, 2, 10, 0));
        setCreatedAt(latestRecruit, LocalDateTime.of(2026, 8, 3, 10, 0));
        entityManager.clear();

        // when
        List<Recruit> result = recruitRepository.findAllForMailDispatch();

        // then
        assertThat(result)
                .extracting(Recruit::getId)
                .containsExactly(latestRecruit.getId(), secondTieRecruit.getId(), firstTieRecruit.getId(), oldestRecruit.getId());
        assertThat(org.hibernate.Hibernate.isInitialized(result.getFirst().getSemester())).isTrue();
    }

    @Test
    void 허용되지_않은_모집_유형과_모집_사유_조합은_저장할_수_없다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());
        Recruit recruit = Recruit.builder()
                .semester(savedSemester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .recruitType(RecruitType.MAKERS)
                .recruitTypeDetail(RecruitTypeDetail.REGULAR)
                .build();

        // when & then
        assertThatThrownBy(() -> recruitRepository.saveAndFlush(recruit))
                .isInstanceOf(RecruitException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.INVALID_RECRUIT_TYPE_DETAIL);
    }

    private Recruit saveRecruit(Semester semester, JobFamily jobFamily) {
        return recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .build());
    }

    private Semester saveSemester(String name) {
        return semesterRepository.save(Semester.builder()
                .name(name)
                .isRecruiting(true)
                .build());
    }

    private Recruit saveRecruit(Semester semester, JobFamily jobFamily, RecruitType recruitType) {
        return recruitRepository.save(Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .recruitType(recruitType)
                .recruitTypeDetail(recruitType == RecruitType.SEMESTER
                        ? RecruitTypeDetail.REGULAR
                        : RecruitTypeDetail.NEW)
                .build());
    }

    private Apply saveApply(
            Recruit recruit,
            ApplyStatus status,
            SelectionResult selectionResult,
            String email
    ) {
        Applicant applicant = applicantRepository.save(Applicant.builder()
                .email(email)
                .semesterId(recruit.getSemester().getId())
                .jobFamily(recruit.getJobFamily())
                .role(Role.APPLY)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build());
        return applyRepository.save(Apply.builder()
                .applicant(applicant)
                .recruit(recruit)
                .status(status)
                .selectionResult(selectionResult)
                .build());
    }

    private ApplyManagementRecruitSearchCondition searchCondition(
            RecruitType recruitType,
            Set<JobFamily> jobFamilies,
            Long cursor
    ) {
        return new ApplyManagementRecruitSearchCondition(
                ApplyStatus.SUBMITTED,
                recruitType,
                jobFamilies,
                cursor,
                20);
    }

    private void setCreatedAt(Recruit recruit, LocalDateTime createdAt) {
        entityManager.createNativeQuery("UPDATE recruit SET created_at = :createdAt WHERE id = :recruitId")
                .setParameter("createdAt", createdAt)
                .setParameter("recruitId", recruit.getId())
                .executeUpdate();
        ReflectionTestUtils.setField(recruit, "createdAt", createdAt);
    }
}
