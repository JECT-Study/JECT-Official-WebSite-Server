package org.ject.support.domain.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.exception.ApplicantErrorCode;
import org.ject.support.domain.applicant.exception.ApplicantException;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.applicant.service.ApplicantCommandService;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
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
@Import({QueryDslTestConfig.class, ApplyService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TempApplyDeleteIntegrationTest extends TestSupport {

    @Autowired
    private ApplyService service;
    @Autowired
    private ApplyRepository applyRepository;
    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockitoBean
    private ApplicantCommandService applicantCommandService;
    @MockitoBean
    private Map2JsonSerializer map2JsonSerializer;
    @MockitoBean
    private String2MapSerializer string2MapSerializer;

    private TransactionTemplate transactions;
    private Long applicantId;
    private Long applyId;
    private Long recruitId;
    private Long formId;
    private Long portfolioId;

    @BeforeEach
    void setUp() {
        transactions = new TransactionTemplate(transactionManager);
        transactions.executeWithoutResult(status -> {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            Semester semester = Semester.builder().name("임시삭제" + suffix).build();
            entityManager.persist(semester);
            Applicant applicant = Applicant.builder().email(suffix + "@test.com").name("김젝트")
                    .phoneNumber("01012345678").semesterId(semester.getId()).role(Role.APPLY).build();
            entityManager.persist(applicant);
            Recruit recruit = Recruit.builder().title("임시저장 삭제 검증").semester(semester).jobFamily(JobFamily.BE)
                    .recruitType(RecruitType.SEMESTER).recruitTypeDetail(RecruitTypeDetail.REGULAR)
                    .startDate(LocalDateTime.now().minusDays(1)).endDate(LocalDateTime.now().plusDays(1)).build();
            entityManager.persist(recruit);
            Apply apply = Apply.createApply(applicant, recruit);
            ApplicationForm form = ApplicationForm.builder().apply(apply).content("{\"1\":\"기존 답변\"}").build();
            Portfolio portfolio = Portfolio.builder().applicationForm(form).fileName("test.pdf")
                    .fileUrl("https://example.invalid/test.pdf").fileSize(100).sequence(1).build();
            form.getPortfolios().add(portfolio);
            apply.updateApplicationForm(form);
            apply.saveTemporarily();
            entityManager.persist(apply);
            entityManager.flush();
            applicantId = applicant.getId();
            applyId = apply.getId();
            recruitId = recruit.getId();
            formId = form.getId();
            portfolioId = portfolio.getId();
        });
    }

    @Test
    void 임시저장_지원서를_삭제하면_지원자와_첨부도_함께_삭제한다() {
        // given
        doAnswer(invocation -> {
            applicantRepository.deleteById(invocation.getArgument(0, Long.class));
            return null;
        }).when(applicantCommandService).deleteApplicant(applicantId);

        // when
        service.deleteProfileAndTempApplicationForm(applicantId, recruitId);

        // then
        transactions.executeWithoutResult(status -> {
            assertThat(applyRepository.findById(applyId)).isEmpty();
            assertThat(applicantRepository.findById(applicantId)).isEmpty();
            assertThat(deletedFlag("apply", applyId)).isEqualTo(1);
            assertThat(deletedFlag("applicant", applicantId)).isEqualTo(1);
            assertThat(entityManager.find(ApplicationForm.class, formId)).isNull();
            assertThat(entityManager.find(Portfolio.class, portfolioId)).isNull();
        });
    }

    @Test
    void 지원자_삭제가_실패하면_지원서와_포트폴리오_삭제도_롤백한다() {
        // given
        doAnswer(invocation -> {
            applicantRepository.deleteById(invocation.getArgument(0, Long.class));
            entityManager.flush();
            throw new ApplicantException(ApplicantErrorCode.NOT_FOUND_APPLICANT);
        }).when(applicantCommandService).deleteApplicant(anyLong());

        // when, then
        assertThatThrownBy(() -> service.deleteProfileAndTempApplicationForm(applicantId, recruitId))
                .isInstanceOf(ApplicantException.class);
        transactions.executeWithoutResult(status -> {
            Apply apply = applyRepository.findById(applyId).orElseThrow();
            assertThat(apply.isTempSaved()).isTrue();
            assertThat(applicantRepository.findById(applicantId)).isPresent();
            assertThat(deletedFlag("apply", applyId)).isZero();
            assertThat(deletedFlag("applicant", applicantId)).isZero();
            assertThat(entityManager.find(ApplicationForm.class, formId).getContent()).contains("기존 답변");
            assertThat(entityManager.find(Portfolio.class, portfolioId)).isNotNull();
        });
    }

    private int deletedFlag(String table, Long id) {
        Object value = entityManager.createNativeQuery("SELECT is_deleted FROM " + table + " WHERE id = :id")
                .setParameter("id", id).getSingleResult();
        return value instanceof Boolean flag ? (flag ? 1 : 0) : ((Number) value).intValue();
    }
}
