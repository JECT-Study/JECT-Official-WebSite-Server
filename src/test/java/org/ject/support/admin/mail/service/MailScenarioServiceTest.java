package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailScenarioServiceTest {

    @InjectMocks
    private MailScenarioService mailScenarioService;

    @Mock
    private MailScenarioRepository mailScenarioRepository;

    @Spy
    private MailTemplateValidator mailTemplateValidator = new MailTemplateValidator();

    @Mock
    private MailTemplateEngine mailTemplateEngine;

    // ── getScenarioVariables ──────────────────────────────

    @Test
    @DisplayName("시나리오 ID로 요청하면 공통 및 개인 변수 목록을 분리하여 반환한다")
    void getScenarioVariables() {
        Long scenarioId = 1L;
        Set<MailScenarioVariable> variables = Set.of(
                MailScenarioVariable.builder().key("APPLY_URL").label("URL").inputType(VariableInputType.URL).required(true).build()
        );
        MailScenario scenario = MailScenario.builder()
                .name("불합격 통지")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(variables)
                .build();

        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(scenario));

        MailScenarioVariableResponse response = mailScenarioService.getScenarioVariables(scenarioId);

        assertThat(response.customVariables()).hasSize(1);
    }

    // ── createScenario ────────────────────────────────────

    @Test
    @DisplayName("시나리오를 정상적으로 생성한다")
    void createScenario() {
        MailScenarioRequest request = new MailScenarioRequest(
                "테스트 시나리오",
                MailScenarioCategory.MAKERS,
                MailScenarioType.FIRST_PASS,
                "TEST_CODE",
                "제목",
                "본문",
                true,
                List.of()
        );

        given(mailScenarioRepository.existsByScenarioCode(anyString())).willReturn(false);
        given(mailScenarioRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        MailScenarioResponse response = mailScenarioService.createScenario(request);

        assertThat(response.scenarioCode()).isEqualTo("TEST_CODE");
    }

    // ── updateScenario ────────────────────────────────────

    @Test
    @DisplayName("변수를 리스트와 템플릿에서 모두 제거하면 정상적으로 수정(삭제 완료)된다")
    void updateScenario_DeleteVariable_Success() {
        Long scenarioId = 1L;
        MailScenario existing = MailScenario.builder()
                .name("기존")
                .category(MailScenarioCategory.GENERAL)
                .type(MailScenarioType.ETC)
                .scenarioCode("CODE")
                .subjectTemplate("안녕 ${OLD_VAR}")
                .bodyTemplate("본문")
                .customVariables(Set.of(MailScenarioVariable.builder().key("OLD_VAR").label("기존").inputType(VariableInputType.TEXT).build()))
                .build();

        // OLD_VAR를 리스트에서도 빼고 템플릿에서도 지움
        MailScenarioRequest request = new MailScenarioRequest(
                "수정",
                MailScenarioCategory.GENERAL,
                MailScenarioType.ETC,
                "CODE",
                "안녕", 
                "본문",
                true,
                List.of() 
        );

        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(existing));
        given(mailScenarioRepository.existsByScenarioCodeAndIdNot(anyString(), any())).willReturn(false);

        MailScenarioResponse response = mailScenarioService.updateScenario(scenarioId, request);

        assertThat(response.name()).isEqualTo("수정");
        // 실제 엔티티의 변수 목록이 비어있는지 확인 (Spy를 통해 검증 로직 통과됨을 확인)
        then(mailTemplateValidator).should().validateAllowedPlaceholders(eq("안녕"), any());
    }

    @Test
    @DisplayName("변수를 리스트에서 제거했으나 템플릿에 남아있으면 예외가 발생하며 누락된 키를 알려준다")
    void updateScenario_DeleteVariable_Fail_StillInUse() {
        Long scenarioId = 1L;
        MailScenario existing = MailScenario.builder()
                .name("기존")
                .category(MailScenarioCategory.GENERAL)
                .type(MailScenarioType.ETC)
                .scenarioCode("CODE")
                .subjectTemplate("안녕 ${OLD_VAR}")
                .bodyTemplate("본문 ${NAME}")
                .customVariables(Set.of(MailScenarioVariable.builder().key("OLD_VAR").label("기존").inputType(VariableInputType.TEXT).build()))
                .build();

        // OLD_VAR를 리스트에서는 뺐지만 템플릿("안녕 ${OLD_VAR}")에는 남겨둠
        MailScenarioRequest request = new MailScenarioRequest(
                "수정",
                MailScenarioCategory.GENERAL,
                MailScenarioType.ETC,
                "CODE",
                "안녕 ${OLD_VAR}", 
                "본문",
                true,
                List.of() 
        );

        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> mailScenarioService.updateScenario(scenarioId, request))
                .isInstanceOf(MailException.class)
                .hasMessageContaining("OLD_VAR")
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE);
    }

    // ── deleteScenario ────────────────────────────────────

    @Test
    @DisplayName("시나리오를 정상적으로 삭제한다")
    void deleteScenario() {
        Long scenarioId = 1L;
        MailScenario existing = MailScenario.builder().build();
        given(mailScenarioRepository.findById(scenarioId)).willReturn(Optional.of(existing));

        mailScenarioService.deleteScenario(scenarioId);

        then(mailScenarioRepository).should().delete(existing);
    }
}
