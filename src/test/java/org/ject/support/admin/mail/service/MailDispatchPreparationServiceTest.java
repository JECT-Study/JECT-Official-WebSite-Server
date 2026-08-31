package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class MailDispatchPreparationServiceTest extends UnitTestSupport {

    @Mock
    private MailScenarioRepository mailScenarioRepository;

    @Mock
    private RecruitRepository recruitRepository;

    @Mock
    private ApplyRepository applyRepository;

    private MailDispatchPreparationService mailDispatchPreparationService;

    @BeforeEach
    void setUp() {
        MailTemplateRenderService renderService = new MailTemplateRenderService(
                new MailTemplateEngine(), new MailTemplateValidator());
        mailDispatchPreparationService = new MailDispatchPreparationService(
                mailScenarioRepository, recruitRepository, applyRepository, renderService);
    }

    @Test
    @DisplayName("유효한 대상과 입력값으로 발송 계획을 생성한다")
    void 유효한_대상과_입력값으로_발송_계획을_생성한다() {
        // given
        MailScenario scenario = scenario("${name}", "안녕하세요 ${MESSAGE}");
        Apply apply = apply(20L, 2L, "applicant@ject.kr", "홍길동");
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(recruitRepository.existsById(2L)).willReturn(true);
        given(applyRepository.findAllByIdAndStatusWithApplicantRecruitAndForm(
                List.of(20L), ApplyStatus.SUBMITTED)).willReturn(List.of(apply));

        // when
        MailDispatchPlan plan = mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), "  ${name}  ", Map.of("MESSAGE", "반갑습니다")),
                3L,
                "dispatch-key");

        // then
        assertThat(plan.scenarioId()).isEqualTo(1L);
        assertThat(plan.recruitId()).isEqualTo(2L);
        assertThat(plan.requestedByAdminId()).isEqualTo(3L);
        assertThat(plan.idempotencyKey()).isEqualTo("dispatch-key");
        assertThat(plan.subjectTemplate()).isEqualTo("  ${name}  ");
        assertThat(plan.targets()).singleElement().satisfies(target -> {
            assertThat(target.applyId()).isEqualTo(20L);
            assertThat(target.email()).isEqualTo("applicant@ject.kr");
            assertThat(target.subject()).isEqualTo("홍길동");
            assertThat(target.body()).isEqualTo("안녕하세요 반갑습니다");
        });
    }

    @Test
    @DisplayName("중복된 지원 ID가 있으면 발송 계획을 생성하지 않는다")
    void 중복된_지원_ID가_있으면_발송_계획을_생성하지_않는다() {
        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L, 20L), null, Map.of()),
                3L, "dispatch-key"))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.DUPLICATE_APPLY_ID);
    }

    @Test
    @DisplayName("존재하지 않는 모집 공고면 발송 계획을 생성하지 않는다")
    void 존재하지_않는_모집_공고면_발송_계획을_생성하지_않는다() {
        // given
        given(recruitRepository.existsById(2L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), null, Map.of()),
                3L, "dispatch-key"))
                .isInstanceOf(RecruitException.class)
                .extracting("errorCode")
                .isEqualTo(RecruitErrorCode.NOT_FOUND_RECRUIT);
    }

    @Test
    @DisplayName("다른 모집 공고의 지원이 포함되면 발송 계획을 생성하지 않는다")
    void 다른_모집_공고의_지원이_포함되면_발송_계획을_생성하지_않는다() {
        // given
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario("제목", "본문")));
        given(recruitRepository.existsById(2L)).willReturn(true);
        given(applyRepository.findAllByIdAndStatusWithApplicantRecruitAndForm(
                List.of(20L), ApplyStatus.SUBMITTED)).willReturn(List.of(apply(20L, 99L,
                "applicant@ject.kr", "홍길동")));

        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), null, Map.of()),
                3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_DISPATCH_TARGETS);
    }

    @Test
    @DisplayName("비활성 시나리오면 발송 계획을 생성하지 않는다")
    void 비활성_시나리오면_발송_계획을_생성하지_않는다() {
        // given
        given(recruitRepository.existsById(2L)).willReturn(true);
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(
                MailScenario.builder()
                        .name("비활성")
                        .subjectTemplate("제목")
                        .bodyTemplate("본문")
                        .active(false)
                        .customVariables(Set.of())
                        .build()));

        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), null, Map.of()),
                3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INACTIVE_SCENARIO);
    }

    @Test
    @DisplayName("제목이 공백을 제외하고 2자 미만이면 발송 계획을 생성하지 않는다")
    void 제목이_공백을_제외하고_2자_미만이면_발송_계획을_생성하지_않는다() {
        // given
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario("제목", "본문")));
        given(recruitRepository.existsById(2L)).willReturn(true);
        given(applyRepository.findAllByIdAndStatusWithApplicantRecruitAndForm(
                List.of(20L), ApplyStatus.SUBMITTED)).willReturn(List.of(apply(20L, 2L,
                "applicant@ject.kr", "홍길동")));

        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), "  ?  ", Map.of()),
                3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_SUBJECT);
    }

    @Test
    @DisplayName("제목이 공백을 제외하고 40자를 초과하면 발송 계획을 생성하지 않는다")
    void 제목이_공백을_제외하고_40자를_초과하면_발송_계획을_생성하지_않는다() {
        // given
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario("제목", "본문")));
        given(recruitRepository.existsById(2L)).willReturn(true);
        given(applyRepository.findAllByIdAndStatusWithApplicantRecruitAndForm(
                List.of(20L), ApplyStatus.SUBMITTED)).willReturn(List.of(apply(20L, 2L,
                "applicant@ject.kr", "홍길동")));

        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), "가".repeat(41), Map.of()),
                3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_SUBJECT);
    }

    @Test
    @DisplayName("제목에 공백이 포함되어도 공백을 제외한 길이로 검증한다")
    void 제목에_공백이_포함되어도_공백을_제외한_길이로_검증한다() {
        // given
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario("제목", "본문")));
        given(recruitRepository.existsById(2L)).willReturn(true);
        given(applyRepository.findAllByIdAndStatusWithApplicantRecruitAndForm(
                List.of(20L), ApplyStatus.SUBMITTED)).willReturn(List.of(apply(20L, 2L,
                "applicant@ject.kr", "홍길동")));

        // when
        MailDispatchPlan plan = mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), "가 ".repeat(40), Map.of()),
                3L,
                "dispatch-key");

        // then
        assertThat(plan.targets()).singleElement()
                .extracting(MailDispatchPlan.Target::subject)
                .isEqualTo("가 ".repeat(40).trim());
    }

    @Test
    @DisplayName("시스템 변수를 입력값으로 덮어쓰면 발송 계획을 생성하지 않는다")
    void 시스템_변수를_입력값으로_덮어쓰면_발송_계획을_생성하지_않는다() {
        // given
        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario("제목", "본문")));
        given(recruitRepository.existsById(2L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> mailDispatchPreparationService.prepare(
                new SendMailDispatchRequest(2L, 1L, List.of(20L), null, Map.of("name", "운영자")),
                3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE);
    }

    private MailScenario scenario(String subject, String body) {
        return MailScenario.builder()
                .name("발송 시나리오")
                .subjectTemplate(subject)
                .bodyTemplate(body)
                .active(true)
                .customVariables(Set.of(messageVariable()))
                .build();
    }

    private MailScenarioVariable messageVariable() {
        return MailScenarioVariable.builder()
                .key("MESSAGE")
                .label("메시지")
                .inputType(VariableInputType.TEXT)
                .required(false)
                .build();
    }

    private Apply apply(Long id, Long recruitId, String email, String name) {
        return Apply.builder()
                .id(id)
                .applicant(Applicant.builder().email(email).name(name).build())
                .recruit(Recruit.builder()
                        .id(recruitId)
                        .semester(Semester.builder().name("10기").build())
                        .build())
                .applicationForm(ApplicationForm.builder().build())
                .status(ApplyStatus.SUBMITTED)
                .selectionResult(SelectionResult.PASSED)
                .build();
    }
}
