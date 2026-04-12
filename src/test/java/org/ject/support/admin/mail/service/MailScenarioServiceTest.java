package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioRequest.CustomVariableRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class MailScenarioServiceTest {

    @InjectMocks
    private MailScenarioService mailScenarioService;

    @Mock
    private MailScenarioRepository mailScenarioRepository;

    @Mock
    private MailTemplateValidator mailTemplateValidator;

    @Mock
    private MailTemplateEngine mailTemplateEngine;

    // ── getScenarioVariables ──────────────────────────────

    @Test
    @DisplayName("시나리오 ID로 요청하면 공통 및 개인 변수 목록을 분리하여 반환한다")
    void getScenarioVariables() {
        Long scenarioId = 1L;
        Set<MailScenarioVariable> variables = Set.of(
                MailScenarioVariable.builder().key("RECRUIT_ALERT_APPLY_URL").label("지원 링크").inputType(VariableInputType.URL).required(true).build()
        );
        MailScenario scenario = MailScenario.builder()
                .name("일반 구성원 - 불합격 통지")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME} 결과 안내")
                .bodyTemplate("안녕하세요 ${name}님, ${RECRUIT_ALERT_APPLY_URL}")
                .active(true)
                .customVariables(variables)
                .build();

        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(scenario));

        MailScenarioVariableResponse response = mailScenarioService.getScenarioVariables(scenarioId);

        assertThat(response.name()).isEqualTo("일반 구성원 - 불합격 통지");
        assertThat(response.customVariables()).hasSize(1);
        assertThat(response.personalVariables()).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 시나리오 ID를 요청하면 예외가 발생한다")
    void getScenarioVariables_NotFound() {
        Long notFoundId = 999L;
        given(mailScenarioRepository.findById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mailScenarioService.getScenarioVariables(notFoundId))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.SCENARIO_NOT_FOUND);
    }

    // ── createScenario ────────────────────────────────────

    @Test
    @DisplayName("시나리오를 정상적으로 생성한다")
    void createScenario() {
        MailScenarioRequest request = new MailScenarioRequest(
                "메이커스 1차 합격",
                MailScenarioCategory.MAKERS,
                MailScenarioType.FIRST_PASS,
                "MAKERS_FIRST_PASS",
                "[JECT] ${RECRUIT_NAME} 1차 합격",
                "${name}님 축하드립니다.",
                true,
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );

        MailScenario saved = MailScenario.builder()
                .name(request.name())
                .category(request.category())
                .type(request.type())
                .scenarioCode(request.scenarioCode())
                .subjectTemplate(request.subjectTemplate())
                .bodyTemplate(request.bodyTemplate())
                .active(request.active())
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();

        given(mailScenarioRepository.existsByScenarioCode(request.scenarioCode())).willReturn(false);
        given(mailScenarioRepository.saveAndFlush(any(MailScenario.class))).willReturn(saved);

        MailScenarioResponse response = mailScenarioService.createScenario(request);

        assertThat(response.name()).isEqualTo("메이커스 1차 합격");
        assertThat(response.scenarioCode()).isEqualTo("MAKERS_FIRST_PASS");
    }

    @Test
    @DisplayName("중복된 시나리오 코드는 생성할 수 없다")
    void createScenario_DuplicateScenarioCode() {
        MailScenarioRequest request = new MailScenarioRequest(
                "중복 테스트",
                MailScenarioCategory.GENERAL,
                MailScenarioType.ETC,
                "DUPLICATE_CODE",
                "[JECT] ${RECRUIT_NAME}",
                "${name}",
                true,
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );
        given(mailScenarioRepository.existsByScenarioCode("DUPLICATE_CODE")).willReturn(true);

        assertThatThrownBy(() -> mailScenarioService.createScenario(request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.DUPLICATE_SCENARIO_CODE);
    }

    @Test
    @DisplayName("템플릿 문법이 잘못되면 시나리오 생성에 실패한다")
    void createScenario_InvalidTemplateSyntax() {
        MailScenarioRequest request = new MailScenarioRequest(
                "문법 오류",
                MailScenarioCategory.GENERAL,
                MailScenarioType.ETC,
                "INVALID_SYNTAX",
                "[JECT] ${RECRUIT_NAME",
                "${name}",
                true,
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );
        given(mailScenarioRepository.existsByScenarioCode("INVALID_SYNTAX")).willReturn(false);
        doThrow(new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX))
                .when(mailTemplateValidator).validateSyntax(request.subjectTemplate());

        assertThatThrownBy(() -> mailScenarioService.createScenario(request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
    }

    @Test
    @DisplayName("허용되지 않은 변수가 포함되면 시나리오 생성에 실패한다")
    void createScenario_UnsupportedTemplateVariable() {
        MailScenarioRequest request = new MailScenarioRequest(
                "변수 오류",
                MailScenarioCategory.GENERAL,
                MailScenarioType.ETC,
                "UNSUPPORTED_VAR",
                "[JECT] ${UNKNOWN_KEY}",
                "${name}",
                true,
                List.of()
        );
        given(mailScenarioRepository.existsByScenarioCode("UNSUPPORTED_VAR")).willReturn(false);
        doThrow(new MailException(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE))
                .when(mailTemplateValidator).validateAllowedPlaceholders(eq(request.subjectTemplate()), any());

        assertThatThrownBy(() -> mailScenarioService.createScenario(request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE);
    }

    @Test
    @DisplayName("동시 요청으로 DB unique 제약 위반이 발생하면 중복 코드 예외로 변환한다")
    void createScenario_DuplicateScenarioCode_RaceCondition() {
        MailScenarioRequest request = new MailScenarioRequest(
                "동시성 테스트",
                MailScenarioCategory.GENERAL,
                MailScenarioType.ETC,
                "RACE_CODE",
                "[JECT] ${RECRUIT_NAME}",
                "${name}",
                true,
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );

        given(mailScenarioRepository.existsByScenarioCode("RACE_CODE")).willReturn(false);
        given(mailScenarioRepository.saveAndFlush(any(MailScenario.class)))
                .willThrow(duplicateScenarioCodeViolation());

        assertThatThrownBy(() -> mailScenarioService.createScenario(request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.DUPLICATE_SCENARIO_CODE);
    }

    // ── updateScenario ────────────────────────────────────

    @Test
    @DisplayName("시나리오를 정상적으로 수정한다")
    void updateScenario() {
        Long scenarioId = 1L;
        MailScenario existing = MailScenario.builder()
                .name("기존 시나리오")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.ETC)
                .scenarioCode("MEMBER_OLD")
                .subjectTemplate("[JECT] 기존 제목")
                .bodyTemplate("기존 본문")
                .active(true)
                .customVariables(Set.of())
                .build();
        MailScenarioRequest request = new MailScenarioRequest(
                "새 이름",
                MailScenarioCategory.CLUB_MEMBER,
                MailScenarioType.FINAL_PASS,
                "MEMBER_NEW",
                "[JECT] ${RECRUIT_NAME}",
                "${name}",
                false,
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );

        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(existing));
        given(mailScenarioRepository.existsByScenarioCodeAndIdNot(request.scenarioCode(), scenarioId)).willReturn(false);
        given(mailScenarioRepository.saveAndFlush(existing)).willReturn(existing);

        MailScenarioResponse response = mailScenarioService.updateScenario(scenarioId, request);

        assertThat(response.name()).isEqualTo("새 이름");
        assertThat(response.scenarioCode()).isEqualTo("MEMBER_NEW");
        assertThat(response.active()).isFalse();
    }

    @Test
    @DisplayName("수정 시 DB unique 제약 위반이 발생하면 중복 코드 예외로 변환한다")
    void updateScenario_DuplicateScenarioCode_RaceCondition() {
        Long scenarioId = 1L;
        MailScenario existing = MailScenario.builder()
                .name("기존 시나리오")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.ETC)
                .scenarioCode("MEMBER_OLD")
                .subjectTemplate("[JECT] 기존 제목")
                .bodyTemplate("기존 본문")
                .active(true)
                .customVariables(Set.of())
                .build();
        MailScenarioRequest request = new MailScenarioRequest(
                "새 이름",
                MailScenarioCategory.CLUB_MEMBER,
                MailScenarioType.FINAL_PASS,
                "MEMBER_NEW",
                "[JECT] ${RECRUIT_NAME}",
                "${name}",
                false,
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );

        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(existing));
        given(mailScenarioRepository.existsByScenarioCodeAndIdNot(request.scenarioCode(), scenarioId)).willReturn(false);
        given(mailScenarioRepository.saveAndFlush(existing))
                .willThrow(duplicateScenarioCodeViolation());

        assertThatThrownBy(() -> mailScenarioService.updateScenario(scenarioId, request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.DUPLICATE_SCENARIO_CODE);
    }

    // ── deleteScenario ────────────────────────────────────

    @Test
    @DisplayName("시나리오를 정상적으로 삭제한다")
    void deleteScenario() {
        Long scenarioId = 1L;
        MailScenario existing = MailScenario.builder()
                .name("삭제 대상")
                .category(MailScenarioCategory.GENERAL)
                .type(MailScenarioType.ETC)
                .scenarioCode("DELETE_ME")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(Set.of())
                .build();
        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(existing));

        mailScenarioService.deleteScenario(scenarioId);

        then(mailScenarioRepository).should().delete(existing);
    }

    // ── renderScenario ────────────────────────────────────

    @Test
    @DisplayName("렌더링 시 필수 공통 변수를 검증하고 본문 템플릿을 치환한다")
    void renderScenario() {
        Long scenarioId = 1L;
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME}")
                .bodyTemplate("안녕하세요 ${name}님")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(scenario));
        given(mailTemplateEngine.render(anyString(), any())).willReturn("안녕하세요 젝트님");

        String result = mailScenarioService.renderScenario(scenarioId, Map.of(
                "NAME", "젝트",
                "RECRUIT_NAME", "메이커스 5기"
        ));

        assertThat(result).isEqualTo("안녕하세요 젝트님");
    }

    @Test
    @DisplayName("전체 시나리오 목록을 반환한다")
    void getScenarios() {
        List<MailScenario> scenarios = List.of(
                MailScenario.builder()
                        .name("시나리오 A")
                        .category(MailScenarioCategory.GENERAL)
                        .type(MailScenarioType.ETC)
                        .scenarioCode("CODE_A")
                        .subjectTemplate("subject")
                        .bodyTemplate("body")
                        .active(true)
                        .customVariables(Set.of())
                        .build(),
                MailScenario.builder()
                        .name("시나리오 B")
                        .category(MailScenarioCategory.GENERAL)
                        .type(MailScenarioType.ETC)
                        .scenarioCode("CODE_B")
                        .subjectTemplate("subject")
                        .bodyTemplate("body")
                        .active(true)
                        .customVariables(Set.of())
                        .build()
        );
        given(mailScenarioRepository.findAll()).willReturn(scenarios);

        List<MailScenarioResponse> result = mailScenarioService.getScenarios();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MailScenarioResponse::scenarioCode)
                .containsExactlyInAnyOrder("CODE_A", "CODE_B");
    }

    private DataIntegrityViolationException duplicateScenarioCodeViolation() {
        ConstraintViolationException cause = new ConstraintViolationException(
                "duplicate scenario code",
                new SQLIntegrityConstraintViolationException("duplicate", "23000"),
                "insert into mail_scenario ...",
                "uk_mail_scenario_scenario_code"
        );
        return new DataIntegrityViolationException("constraint violated", cause);
    }
}
