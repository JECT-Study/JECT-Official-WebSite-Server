package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.ject.support.admin.apply.dto.EditApplyRequest;
import org.ject.support.admin.apply.dto.EditWaitlistOrderRequest;
import org.ject.support.admin.apply.dto.UpdateSelectionResultsRequest;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.service.ApplicantCommandService;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@Import({QueryDslTestConfig.class, AdminApplyService.class, Map2JsonSerializer.class,
        String2MapSerializer.class, ObjectMapperConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminApplyEditIntegrationTest extends TestSupport {

    @Autowired
    private AdminApplyService service;
    @Autowired
    private AdminApplyRepository repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockitoBean
    private ApplicantCommandService applicantCommandService;

    private TransactionTemplate transactions;
    private Long applyId;
    private Long questionId;
    private Long deletePortfolioId;
    private Long keepPortfolioId;

    @BeforeEach
    void setUp() {
        transactions = new TransactionTemplate(transactionManager);
        transactions.executeWithoutResult(status -> {
            Semester semester = Semester.builder().name("수정검증" + UUID.randomUUID().toString().substring(0, 8)).build();
            entityManager.persist(semester);
            Applicant applicant = Applicant.builder().name("김젝트").phoneNumber("01012345678")
                    .email("before@test.com").semesterId(semester.getId()).role(Role.APPLY).jobFamily(JobFamily.BE)
                    .careerDetails(CareerDetails.STUDENT).region(Region.SEOUL).experiencePeriod(ExperiencePeriod.NONE)
                    .interestedDomains(List.of("GAME")).build();
            entityManager.persist(applicant);
            Recruit recruit = Recruit.builder().semester(semester).jobFamily(JobFamily.BE)
                    .recruitType(RecruitType.SEMESTER).recruitTypeDetail(RecruitTypeDetail.REGULAR)
                    .startDate(LocalDateTime.now().minusDays(1)).endDate(LocalDateTime.now().plusDays(1)).build();
            Question question = Question.builder().title("지원 동기").label("동기")
                    .inputType(Question.InputType.TEXT).sequence(1).build();
            recruit.addQuestion(question);
            entityManager.persist(recruit);
            entityManager.flush();
            questionId = question.getId();
            Apply apply = Apply.createApply(applicant, recruit);
            ApplicationForm form = ApplicationForm.builder().apply(apply)
                    .content("{\"" + questionId + "\":\"기존 답변\"}").build();
            Portfolio first = Portfolio.builder().fileUrl("https://example.invalid/first.pdf").fileName("first.pdf")
                    .fileSize(100).sequence(1).applicationForm(form).build();
            Portfolio second = Portfolio.builder().fileUrl("https://example.invalid/second.pdf").fileName("second.pdf")
                    .fileSize(200).sequence(2).applicationForm(form).build();
            form.getPortfolios().addAll(List.of(first, second));
            apply.submit(form);
            apply.editNote("기존 비고");
            apply.decideSelectionResult(SelectionResult.WAITLISTED, 1);
            entityManager.persist(apply);
            entityManager.flush();
            applyId = apply.getId();
            deletePortfolioId = first.getId();
            keepPortfolioId = second.getId();
        });
    }

    @Test
    void 지원서_수정과_포트폴리오_삭제를_함께_저장한다() {
        // given
        EditApplyRequest request = request(Set.of(deletePortfolioId));

        // when
        service.editApply(applyId, request);

        // then
        transactions.executeWithoutResult(status -> {
            Apply saved = repository.findByIdWithApplicant(applyId).orElseThrow();
            assertThat(saved.getApplicant().getName()).isEqualTo(request.name());
            assertThat(saved.getApplicant().getEmail()).isEqualTo(request.email());
            assertThat(saved.getSelectionResult()).isEqualTo(SelectionResult.PASSED);
            assertThat(saved.getWaitlistNumber()).isNull();
            assertThat(saved.getNote()).isEqualTo("새 비고");
            assertThat(saved.getApplicationForm().getContent()).contains("새 답변").doesNotContain("기존 답변");
            assertThat(saved.getApplicationForm().getPortfolios()).extracting(Portfolio::getId)
                    .containsExactly(keepPortfolioId);
            assertThat(entityManager.find(Portfolio.class, deletePortfolioId)).isNull();
        });
    }

    @Test
    void 삭제할_수_없는_포트폴리오가_포함되면_전체_수정을_롤백한다() {
        // given
        Snapshot before = snapshot();
        EditApplyRequest request = request(Set.of(deletePortfolioId, Long.MAX_VALUE));

        // when, then
        assertThatThrownBy(() -> service.editApply(applyId, request)).isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_PORTFOLIO);
        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    void 기존_예비번호를_교환하고_선정결과를_바꾸면_빈_번호_없이_저장한다() {
        // given
        Long secondId = createSecondWaitlistedApply();
        Long recruitId = transactions.execute(status -> repository.findById(applyId).orElseThrow().getRecruit().getId());

        // when
        service.editWaitlistOrder(new EditWaitlistOrderRequest(recruitId, List.of(secondId, applyId)));

        // then
        transactions.executeWithoutResult(status -> {
            assertThat(repository.findById(secondId).orElseThrow().getWaitlistNumber()).isEqualTo(1);
            assertThat(repository.findById(applyId).orElseThrow().getWaitlistNumber()).isEqualTo(2);
        });

        // when
        service.updateSelectionResults(new UpdateSelectionResultsRequest(recruitId, List.of(secondId), SelectionResult.PASSED));

        // then
        transactions.executeWithoutResult(status -> {
            Apply passed = repository.findById(secondId).orElseThrow();
            assertThat(passed.getSelectionResult()).isEqualTo(SelectionResult.PASSED);
            assertThat(passed.getWaitlistNumber()).isNull();
            assertThat(repository.findById(applyId).orElseThrow().getWaitlistNumber()).isEqualTo(1);
        });
    }

    @Test
    void 상세_수정이_실패하면_다른_예비합격자의_번호_당김도_롤백한다() {
        // given
        Long secondId = createSecondWaitlistedApply();
        Snapshot before = snapshot();

        // when, then
        assertThatThrownBy(() -> service.editApply(applyId, request(Set.of(Long.MAX_VALUE))))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_PORTFOLIO);
        assertThat(snapshot()).isEqualTo(before);
        transactions.executeWithoutResult(status ->
                assertThat(repository.findById(secondId).orElseThrow().getWaitlistNumber()).isEqualTo(2));
    }

    @Test
    void 예비합격_지원서를_삭제하면_남은_지원자의_번호를_당겨_저장한다() {
        // given
        Long secondId = createSecondWaitlistedApply();

        // when
        service.deleteApply(applyId);

        // then
        transactions.executeWithoutResult(status -> {
            assertThat(repository.findById(applyId)).isEmpty();
            assertThat(repository.findById(secondId).orElseThrow().getWaitlistNumber()).isEqualTo(1);
        });
    }

    private Long createSecondWaitlistedApply() {
        return transactions.execute(status -> {
            Recruit recruit = repository.findById(applyId).orElseThrow().getRecruit();
            Applicant applicant = Applicant.builder().name("다음지원자").phoneNumber("01012345678")
                    .email("next@test.com").semesterId(recruit.getSemester().getId()).role(Role.APPLY)
                    .jobFamily(JobFamily.BE).careerDetails(CareerDetails.STUDENT).build();
            entityManager.persist(applicant);
            Apply second = Apply.createApply(applicant, recruit);
            second.submit(ApplicationForm.builder().apply(second).content("{}").build());
            second.decideSelectionResult(SelectionResult.WAITLISTED, 2);
            entityManager.persist(second);
            entityManager.flush();
            return second.getId();
        });
    }

    private EditApplyRequest request(Set<Long> ids) {
        return new EditApplyRequest("이수정", "01098765432", "after@test.com", CareerDetails.EMPLOYEE,
                Region.BUSAN, ExperiencePeriod.ONE_TO_TWO, List.of("EDUCATION"), SelectionResult.PASSED,
                "새 비고", Map.of(questionId.toString(), "새 답변"), ids);
    }

    private Snapshot snapshot() {
        return transactions.execute(status -> {
            Apply apply = repository.findByIdWithApplicant(applyId).orElseThrow();
            Applicant applicant = apply.getApplicant();
            return new Snapshot(applicant.getName(), applicant.getPhoneNumber(), applicant.getEmail(),
                    applicant.getCareerDetails(), applicant.getRegion(), applicant.getExperiencePeriod(),
                    List.copyOf(applicant.getInterestedDomains()), apply.getSelectionResult(), apply.getWaitlistNumber(),
                    apply.getNote(), apply.getApplicationForm().getContent(),
                    apply.getApplicationForm().getPortfolios().stream().map(Portfolio::getId).toList());
        });
    }

    private record Snapshot(String name, String phone, String email, CareerDetails career, Region region,
                            ExperiencePeriod experience, List<String> domains, SelectionResult selection,
                            Integer waitlistNumber, String note, String content, List<Long> portfolioIds) {
    }
}
