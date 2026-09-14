package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.PreviewMailRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class MailPreviewServiceTest extends UnitTestSupport {

    private MailPreviewService mailPreviewService;

    @Mock
    private MailScenarioRepository mailScenarioRepository;

    @Mock
    private ApplyRepository applyRepository;

    private final MailTemplateRenderService mailTemplateRenderService = new MailTemplateRenderService(
            new MailTemplateEngine(), new MailTemplateValidator());

    @BeforeEach
    void setUp() {
        mailPreviewService = new MailPreviewService(
                mailScenarioRepository, applyRepository, mailTemplateRenderService);
    }

    @Test
    @DisplayName("지원자 정보와 입력 변수로 제목과 본문을 미리보기한다")
    void 지원자_정보와_입력_변수로_제목과_본문을_미리보기한다() {
        MailScenario scenario = scenario(
                "${name} ${semester} ${waitlistNumber}",
                "면접 일시: ${INTERVIEW_AT}",
                Set.of(dateTimeVariable()),
                true);
        Apply apply = submittedApply(SelectionResult.WAITLISTED, 3);
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(applyRepository.findByIdAndStatusWithApplicant(20L, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(apply));

        MailPreviewResponse response = mailPreviewService.preview(new PreviewMailRequest(
                1L,
                20L,
                Map.of("INTERVIEW_AT", "2026-02-28 10:00")
        ));

        assertThat(response.scenarioId()).isEqualTo(1L);
        assertThat(response.applyId()).isEqualTo(20L);
        assertThat(response.receiverEmail()).isEqualTo("applicant@ject.kr");
        assertThat(response.subject()).isEqualTo("홍길동 10기 3");
        assertThat(response.body()).isEqualTo("면접 일시: 2026-02-28 10:00");
    }

    @Test
    @DisplayName("예비 합격자가 아니면 예비 번호 placeholder를 미해결 오류로 거부한다")
    void 예비_합격자가_아니면_예비_번호_placeholder를_미해결_오류로_거부한다() {
        MailScenario scenario = scenario("제목", "예비 번호: ${waitlistNumber}", Set.of(), true);
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(applyRepository.findByIdAndStatusWithApplicant(20L, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply(SelectionResult.PASSED, null)));

        assertThatThrownBy(() -> mailPreviewService.preview(new PreviewMailRequest(1L, 20L, Map.of())))
                .isInstanceOf(MailException.class)
                .hasMessageContaining("20")
                .hasMessageContaining("waitlistNumber")
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.UNRESOLVED_TEMPLATE_VARIABLE);
    }

    @Test
    @DisplayName("비활성 템플릿은 미리보기할 수 없다")
    void 비활성_템플릿은_미리보기할_수_없다() {
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(
                scenario("제목", "본문", Set.of(), false)));

        assertThatThrownBy(() -> mailPreviewService.preview(new PreviewMailRequest(1L, 20L, Map.of())))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INACTIVE_SCENARIO);
    }

    @Test
    @DisplayName("제출되지 않은 지원은 미리보기할 수 없다")
    void 제출되지_않은_지원은_미리보기할_수_없다() {
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(
                scenario("제목", "본문", Set.of(), true)));
        given(applyRepository.findByIdAndStatusWithApplicant(20L, ApplyStatus.SUBMITTED))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> mailPreviewService.preview(new PreviewMailRequest(1L, 20L, Map.of())))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    @DisplayName("필수 입력 변수가 누락되면 미리보기할 수 없다")
    void 필수_입력_변수가_누락되면_미리보기할_수_없다() {
        MailScenario scenario = scenario("제목", "${MESSAGE}", Set.of(
                MailScenarioVariable.builder()
                        .key("MESSAGE")
                        .label("메시지")
                        .inputType(VariableInputType.TEXT)
                        .required(true)
                        .build()), true);
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(applyRepository.findByIdAndStatusWithApplicant(20L, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply(SelectionResult.PASSED, null)));

        assertThatThrownBy(() -> mailPreviewService.preview(new PreviewMailRequest(1L, 20L, Map.of())))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
    }

    @Test
    @DisplayName("날짜와 시간이 올바르지 않으면 미리보기할 수 없다")
    void 날짜와_시간이_올바르지_않으면_미리보기할_수_없다() {
        MailScenario scenario = scenario("제목", "${INTERVIEW_AT}", Set.of(dateTimeVariable()), true);
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(applyRepository.findByIdAndStatusWithApplicant(20L, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply(SelectionResult.PASSED, null)));

        assertThatThrownBy(() -> mailPreviewService.preview(new PreviewMailRequest(
                1L, 20L, Map.of("INTERVIEW_AT", "2026-02-30 10:00"))))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_VARIABLE_VALUE);
    }

    private MailScenario scenario(String subject,
                                  String body,
                                  Set<MailScenarioVariable> variables,
                                  boolean active) {
        return MailScenario.builder()
                .name("미리보기 템플릿")
                .scenarioCode("PREVIEW")
                .subjectTemplate(subject)
                .bodyTemplate(body)
                .active(active)
                .customVariables(variables)
                .build();
    }

    private MailScenarioVariable dateTimeVariable() {
        return MailScenarioVariable.builder()
                .key("INTERVIEW_AT")
                .label("면접 일시")
                .inputType(VariableInputType.DATE_TIME)
                .required(true)
                .build();
    }

    private Apply submittedApply(SelectionResult selectionResult, Integer waitlistNumber) {
        return Apply.builder()
                .id(20L)
                .applicant(Applicant.builder()
                        .name("홍길동")
                        .email("applicant@ject.kr")
                        .build())
                .recruit(Recruit.builder()
                        .semester(Semester.builder().name("10기").build())
                        .build())
                .applicationForm(ApplicationForm.builder().build())
                .status(ApplyStatus.SUBMITTED)
                .selectionResult(selectionResult)
                .waitlistNumber(waitlistNumber)
                .build();
    }
}
