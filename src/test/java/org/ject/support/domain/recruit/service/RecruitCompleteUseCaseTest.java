package org.ject.support.domain.recruit.service;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.admin.member.dto.request.CreateRecruitMemberRequest;
import org.ject.support.admin.member.service.AdminMemberActivityService;
import org.ject.support.admin.member.service.AdminMemberService;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.service.ApplicantCommandService;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitStatus;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;
import static org.mockito.Mockito.doAnswer;

@DataJpaTest
@Import({QueryDslTestConfig.class, ValidationAutoConfiguration.class, RecruitCompleteUseCase.class,
        RecruitService.class, AdminApplyService.class, AdminMemberService.class, AdminMemberActivityService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RecruitCompleteUseCaseTest extends TestSupport {

    @Autowired
    private RecruitCompleteUseCase useCase;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private MemberRepository memberRepository;
    @MockitoSpyBean
    private AdminMemberActivityService activityService;
    @MockitoSpyBean
    private RecruitService recruitService;
    @MockitoBean
    private ApplicantCommandService applicantCommandService;
    @MockitoBean
    private Map2JsonSerializer map2JsonSerializer;
    @MockitoBean
    private String2MapSerializer string2MapSerializer;
    @MockitoBean
    private RecruitRegisterService recruitRegisterService;

    @ParameterizedTest
    @EnumSource(RecruitType.class)
    void 합격자는_모집_유형에_맞게_등록하고_예비합격자는_불합격으로_확정한다(RecruitType type) {
        // given
        String email = email();
        Long recruitId = transactions().execute(status -> {
            Recruit recruit = recruit(type);
            apply(recruit, email, "합격자", ApplyStatus.SUBMITTED, SelectionResult.PASSED);
            apply(recruit, email(), "예비자", ApplyStatus.SUBMITTED, SelectionResult.WAITLISTED);
            apply(recruit, email(), "불합격자", ApplyStatus.SUBMITTED, SelectionResult.FAILED);
            apply(recruit, email(), "작성자", ApplyStatus.JOINED, SelectionResult.UNDECIDED);
            apply(recruit, email(), "임시저장자", ApplyStatus.TEMP_SAVED, SelectionResult.UNDECIDED);
            return recruit.getId();
        });

        // when
        useCase.completeRecruit(recruitId);

        // then
        transactions().executeWithoutResult(status -> {
            Member member = memberRepository.findByEmail(email).orElseThrow();
            assertThat(member.getName()).isEqualTo("합격자");
            assertThat(member.getRegion()).isEqualTo(Region.SEOUL);
            assertThat(member.getInterestedDomains()).containsExactly("GAME");
            MemberActivity activity = entityManager.createQuery(
                    "select a from MemberActivity a where a.memberId = :id", MemberActivity.class)
                    .setParameter("id", member.getId()).getSingleResult();
            assertThat(activity.getMemberType()).isEqualTo(type.toMemberType());
            assertThat(activity.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
            switch (type) {
                case SEMESTER -> assertThat(activity.getMemberSemester().getSemesterId())
                        .isEqualTo(entityManager.find(Recruit.class, recruitId).getSemester().getId());
                case MAKERS -> assertThat(activity.getMemberMakers()).isNotNull();
                case SUPPORTERS -> assertThat(activity.getMemberSupporters()).isNotNull();
            }
            List<Apply> applies = applies(recruitId);
            assertThat(applies).filteredOn(a -> a.getStatus() == ApplyStatus.SUBMITTED)
                    .extracting(Apply::getSelectionResult)
                    .containsExactly(SelectionResult.PASSED, SelectionResult.FAILED, SelectionResult.FAILED);
            assertThat(applies).allSatisfy(a -> assertThat(a.getWaitlistNumber()).isNull());
            assertThat(applies).filteredOn(a -> a.getStatus() != ApplyStatus.SUBMITTED)
                    .extracting(Apply::getSelectionResult).containsOnly(SelectionResult.UNDECIDED);
            assertThat(entityManager.find(Recruit.class, recruitId).getStatus()).isEqualTo(RecruitStatus.CLOSED);
            assertThat(applies.get(0).getApplicant().getRole()).isEqualTo(Role.APPLY);
        });
        assertThatThrownBy(() -> useCase.completeRecruit(recruitId))
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.ALREADY_CLOSED);
    }

    @Test
    void 같은_이메일의_구성원은_이름이_달라도_이번_지원정보로_덮어쓴다() {
        // given
        String email = email();
        List<Long> ids = transactions().execute(status -> {
            Member existing = memberRepository.save(Member.create("기존이름", email, "01099998888", List.of(), Region.SEOUL));
            Recruit recruit = recruit(RecruitType.SEMESTER);
            apply(recruit, email, "새이름", ApplyStatus.SUBMITTED, SelectionResult.PASSED);
            return List.of(recruit.getId(), existing.getId());
        });

        // when
        useCase.completeRecruit(ids.get(0));

        // then
        transactions().executeWithoutResult(status -> {
            Member member = memberRepository.findByEmail(email).orElseThrow();
            assertThat(member.getId()).isEqualTo(ids.get(1));
            assertThat(member.getName()).isEqualTo("새이름");
            assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
        });
    }

    @Test
    void 제출완료_지원서의_선정이_끝나지_않으면_확정하지_않는다() {
        // given
        Long id = transactions().execute(status -> {
            Recruit recruit = recruit(RecruitType.SEMESTER);
            apply(recruit, email(), "미선정", ApplyStatus.SUBMITTED, SelectionResult.UNDECIDED);
            return recruit.getId();
        });

        // when, then
        assertThatThrownBy(() -> useCase.completeRecruit(id))
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.UNDECIDED_SELECTION_RESULT);
        transactions().executeWithoutResult(status ->
                assertThat(entityManager.find(Recruit.class, id).getStatus()).isEqualTo(RecruitStatus.OPEN));
    }

    @Test
    void 합격자가_없어도_작성중인_지원서는_그대로_두고_공고를_확정한다() {
        // given
        Long id = transactions().execute(status -> {
            Recruit recruit = recruit(RecruitType.SEMESTER);
            apply(recruit, email(), "작성중", ApplyStatus.JOINED, SelectionResult.UNDECIDED);
            apply(recruit, email(), "임시저장", ApplyStatus.TEMP_SAVED, SelectionResult.UNDECIDED);
            return recruit.getId();
        });

        // when
        useCase.completeRecruit(id);

        // then
        transactions().executeWithoutResult(status -> {
            assertThat(entityManager.find(Recruit.class, id).isClosed()).isTrue();
            assertThat(applies(id)).extracting(Apply::getStatus)
                    .containsExactly(ApplyStatus.JOINED, ApplyStatus.TEMP_SAVED);
        });
    }

    @Test
    void 기존_활동이_중복되면_모집확정을_중단한다() {
        // given
        String first = email();
        Long id = transactions().execute(status -> {
            Recruit recruit = recruit(RecruitType.SEMESTER);
            apply(recruit, first, "첫합격자", ApplyStatus.SUBMITTED, SelectionResult.PASSED);
            Member member = memberRepository.save(Member.create("기존구성원", first, "01012345678", List.of(), Region.SEOUL));
            entityManager.persist(semesterActivity().memberId(member.getId())
                    .semesterId(recruit.getSemester().getId()).teamId(null).build());
            return recruit.getId();
        });

        // when, then
        assertThatThrownBy(() -> useCase.completeRecruit(id))
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.RECRUIT_COMPLETION_FAILED);
        transactions().executeWithoutResult(status -> {
            assertThat(memberRepository.findByEmail(first).orElseThrow().getName()).isEqualTo("기존구성원");
            assertThat(entityManager.find(Recruit.class, id).isClosed()).isFalse();
        });
    }

    @Test
    void 뒤의_합격자_등록이_실패하면_앞의_개인정보_수정과_활동_생성도_모두_취소한다() {
        // given
        String first = email();
        String second = email();
        Long id = transactions().execute(status -> {
            memberRepository.save(Member.create("기존이름", first, "01099998888", List.of(), Region.SEOUL));
            Recruit recruit = recruit(RecruitType.SEMESTER);
            apply(recruit, first, "새이름", ApplyStatus.SUBMITTED, SelectionResult.PASSED);
            apply(recruit, second, "실패대상", ApplyStatus.SUBMITTED, SelectionResult.PASSED);
            apply(recruit, email(), "예비자", ApplyStatus.SUBMITTED, SelectionResult.WAITLISTED);
            return recruit.getId();
        });
        doAnswer(invocation -> {
            invocation.callRealMethod();
            CreateRecruitMemberRequest request = invocation.getArgument(0);
            if (request.email().equals(second)) {
                throw new IllegalStateException("등록 실패 검증");
            }
            return null;
        }).when(activityService).createRecruitMemberActivity(any(), any());

        // when, then
        assertThatThrownBy(() -> useCase.completeRecruit(id))
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.RECRUIT_COMPLETION_FAILED);
        transactions().executeWithoutResult(status -> {
            Member member = memberRepository.findByEmail(first).orElseThrow();
            assertThat(member.getName()).isEqualTo("기존이름");
            assertThat(member.getPhoneNumber()).isEqualTo("01099998888");
            assertThat(entityManager.createQuery("select count(a) from MemberActivity a where a.memberId = :id", Long.class)
                    .setParameter("id", member.getId()).getSingleResult()).isZero();
            assertThat(memberRepository.findByEmail(second)).isEmpty();
            assertThat(entityManager.find(Recruit.class, id).isClosed()).isFalse();
            assertThat(applies(id).get(2).getSelectionResult()).isEqualTo(SelectionResult.WAITLISTED);
            assertThat(applies(id).get(2).getWaitlistNumber()).isEqualTo(1);
        });
    }

    @Test
    void 공고_종료에서_실패해도_구성원과_선정_결과를_모두_원복한다() {
        // given
        String email = email();
        Long id = transactions().execute(status -> {
            Recruit recruit = recruit(RecruitType.SEMESTER);
            apply(recruit, email, "합격자", ApplyStatus.SUBMITTED, SelectionResult.PASSED);
            apply(recruit, email(), "예비자", ApplyStatus.SUBMITTED, SelectionResult.WAITLISTED);
            return recruit.getId();
        });
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new IllegalStateException("공고 종료 실패 검증");
        }).when(recruitService).completeRecruit(id);

        // when, then
        assertThatThrownBy(() -> useCase.completeRecruit(id))
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.RECRUIT_COMPLETION_FAILED);
        transactions().executeWithoutResult(status -> {
            assertThat(memberRepository.findByEmail(email)).isEmpty();
            assertThat(entityManager.find(Recruit.class, id).isClosed()).isFalse();
            assertThat(applies(id).get(1).getSelectionResult()).isEqualTo(SelectionResult.WAITLISTED);
            assertThat(applies(id).get(1).getWaitlistNumber()).isEqualTo(1);
        });
    }

    private TransactionTemplate transactions() {
        return new TransactionTemplate(transactionManager);
    }

    private String email() {
        return UUID.randomUUID().toString().substring(0, 12) + "@test.com";
    }

    private Recruit recruit(RecruitType type) {
        Semester semester = null;
        if (type == RecruitType.SEMESTER) {
            semester = Semester.builder().name(UUID.randomUUID().toString().substring(0, 8)).build();
            entityManager.persist(semester);
        }
        Recruit recruit = Recruit.builder().title("모집확정 검증").semester(semester).recruitType(type)
                .recruitTypeDetail(type == RecruitType.SEMESTER ? RecruitTypeDetail.REGULAR : RecruitTypeDetail.NEW)
                .jobFamily(type == RecruitType.SUPPORTERS ? JobFamily.OPS : JobFamily.BE)
                .startDate(LocalDateTime.now().minusDays(2)).endDate(LocalDateTime.now().minusDays(1)).build();
        entityManager.persist(recruit);
        return recruit;
    }

    private Apply apply(Recruit recruit, String email, String name, ApplyStatus status, SelectionResult result) {
        Applicant applicant = Applicant.builder().email(email).name(name).phoneNumber("01012345678")
                .region(Region.SEOUL).careerDetails(CareerDetails.STUDENT).experiencePeriod(ExperiencePeriod.NONE)
                .interestedDomains(List.of("GAME")).role(Role.APPLY).recruitId(recruit.getId())
                .memberType(recruit.getRecruitType().toMemberType())
                .semesterId(recruit.getSemester() == null ? null : recruit.getSemester().getId()).build();
        entityManager.persist(applicant);
        Apply apply = Apply.builder().applicant(applicant).recruit(recruit).status(status).selectionResult(result)
                .waitlistNumber(result == SelectionResult.WAITLISTED ? 1 : null).build();
        if (status != ApplyStatus.JOINED) {
            apply.updateApplicationForm(ApplicationForm.builder().apply(apply).content("{}").build());
        }
        entityManager.persist(apply);
        return apply;
    }

    private List<Apply> applies(Long recruitId) {
        return entityManager.createQuery("select a from Apply a where a.recruit.id = :id order by a.id", Apply.class)
                .setParameter("id", recruitId).getResultList();
    }
}
