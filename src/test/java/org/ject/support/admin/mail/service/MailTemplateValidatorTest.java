package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Set;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MailTemplateValidatorTest {

    private final MailTemplateValidator validator = new MailTemplateValidator();

    @Test
    @DisplayName("정상 placeholder 문법은 통과한다")
    void validateSyntax_Success() {
        assertThatCode(() -> validator.validateSyntax("[JECT] ${RECRUIT_NAME} 안내 ${NAME}"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("닫히지 않은 placeholder는 문법 오류를 발생시킨다")
    void validateSyntax_Fail() {
        assertThatThrownBy(() -> validator.validateSyntax("[JECT] ${RECRUIT_NAME 안내"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
    }

    @Test
    @DisplayName("placeholder 키 형식이 맞지 않으면 문법 오류를 발생시킨다")
    void validateSyntax_InvalidKeyFormat() {
        assertThatThrownBy(() -> validator.validateSyntax("[JECT] ${RECRUIT NAME} 안내"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
    }

    @Test
    @DisplayName("허용 목록에 없는 placeholder는 예외를 발생시키며 누락된 모든 키를 메시지에 포함한다")
    void validateAllowedPlaceholders_Fail_WithMultipleKeys() {
        assertThatThrownBy(() -> validator.validateAllowedPlaceholders(
                "안녕하세요 ${UNKNOWN_KEY_1}님, ${UNKNOWN_KEY_2}를 확인해주세요. ${name}님.",
                Set.of()
        ))
                .isInstanceOf(MailException.class)
                .hasMessageContaining("허용되지 않은 템플릿 변수가 포함되어 있습니다")
                .hasMessageContaining("UNKNOWN_KEY_1")
                .hasMessageContaining("UNKNOWN_KEY_2")
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE);
    }

    @Test
    @DisplayName("허용 변수 검증 시 템플릿이 null이면 문법 오류를 발생시킨다")
    void validateAllowedPlaceholders_NullTemplate() {
        assertThatThrownBy(() -> validator.validateAllowedPlaceholders(null, Set.of()))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
    }

    @Test
    @DisplayName("필수 공통 변수 누락 시 예외를 발생시킨다")
    void 필수_입력_변수가_누락되면_예외를_발생시킨다() {
        assertThatThrownBy(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder().key("RECRUIT_ALERT_APPLY_URL").label("URL").inputType(VariableInputType.URL).required(true).build()),
                Map.of("NAME", "젝트")
        ))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
    }

    @Test
    @DisplayName("필수 공통 변수가 모두 있으면 통과한다")
    void 필수_입력_변수가_모두_있으면_검증을_통과한다() {
        assertThatCode(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder().key("RECRUIT_ALERT_APPLY_URL").label("URL").inputType(VariableInputType.URL).required(true).build()),
                Map.of("RECRUIT_ALERT_APPLY_URL", "https://ject.kr")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("날짜와 시간이 올바른 형식이면 입력 변수 검증을 통과한다")
    void 날짜와_시간이_올바른_형식이면_입력_변수_검증을_통과한다() {
        assertThatCode(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("INTERVIEW_AT")
                        .label("면접 일시")
                        .inputType(VariableInputType.DATE_TIME)
                        .required(true)
                        .build()),
                Map.of("INTERVIEW_AT", "2026-02-28 10:00")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 날짜와 시간이면 입력 변수 오류를 발생시킨다")
    void 존재하지_않는_날짜와_시간이면_입력_변수_오류를_발생시킨다() {
        assertThatThrownBy(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("INTERVIEW_AT")
                        .label("면접 일시")
                        .inputType(VariableInputType.DATE_TIME)
                        .required(true)
                        .build()),
                Map.of("INTERVIEW_AT", "2026-02-30 10:00")
        ))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_VARIABLE_VALUE);
    }

    @Test
    @DisplayName("날짜와 시간이 계약된 형식과 다르면 입력 변수 오류를 발생시킨다")
    void 날짜와_시간이_계약된_형식과_다르면_입력_변수_오류를_발생시킨다() {
        assertThatThrownBy(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("INTERVIEW_AT")
                        .label("면접 일시")
                        .inputType(VariableInputType.DATE_TIME)
                        .required(true)
                        .build()),
                Map.of("INTERVIEW_AT", "2026/02/28 10:00")
        ))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_VARIABLE_VALUE);
    }

    @Test
    @DisplayName("선택 입력 변수의 빈 날짜와 시간은 검증을 통과한다")
    void 선택_입력_변수의_빈_날짜와_시간은_검증을_통과한다() {
        assertThatCode(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("INTERVIEW_AT")
                        .label("면접 일시")
                        .inputType(VariableInputType.DATE_TIME)
                        .required(false)
                        .build()),
                Map.of("INTERVIEW_AT", " ")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("선언되지 않은 입력 변수 키는 입력 변수 오류를 발생시킨다")
    void 선언되지_않은_입력_변수_키는_입력_변수_오류를_발생시킨다() {
        assertThatThrownBy(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("INTERVIEW_AT")
                        .label("면접 일시")
                        .inputType(VariableInputType.DATE_TIME)
                        .required(false)
                        .build()),
                Map.of("UNKNOWN", "값")
        ))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE);
    }

    @Test
    @DisplayName("기존 텍스트 입력 변수는 기존 방식으로 검증을 통과한다")
    void 기존_텍스트_입력_변수는_기존_방식으로_검증을_통과한다() {
        assertThatCode(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("MESSAGE")
                        .label("메시지")
                        .inputType(VariableInputType.TEXT)
                        .required(true)
                        .build()),
                Map.of("MESSAGE", "안내 내용")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("연도가 네 자리보다 길면 입력 변수 오류를 발생시킨다")
    void 연도가_네_자리보다_길면_입력_변수_오류를_발생시킨다() {
        assertThatThrownBy(() -> validator.validateVariables(
                Set.of(MailScenarioVariable.builder()
                        .key("INTERVIEW_AT")
                        .label("면접 일시")
                        .inputType(VariableInputType.DATE_TIME)
                        .required(true)
                        .build()),
                Map.of("INTERVIEW_AT", "+10000-01-01 00:00")
        ))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_VARIABLE_VALUE);
    }
}
