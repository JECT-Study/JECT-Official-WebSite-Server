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
    @DisplayName("허용 목록에 없는 placeholder는 예외를 발생시킨다")
    void validateAllowedPlaceholders_Fail() {
        assertThatThrownBy(() -> validator.validateAllowedPlaceholders(
                "안녕하세요 ${UNKNOWN_KEY}",
                Set.of()
        ))
                .isInstanceOf(MailException.class)
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
    void validateRequiredCommonVariables_Fail() {
        assertThatThrownBy(() -> validator.validateRequiredCommonVariables(
                Set.of(MailScenarioVariable.builder().key("RECRUIT_ALERT_APPLY_URL").label("URL").inputType(VariableInputType.URL).required(true).build()),
                Map.of("NAME", "젝트")
        ))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
    }

    @Test
    @DisplayName("필수 공통 변수가 모두 있으면 통과한다")
    void validateRequiredCommonVariables_Success() {
        assertThatCode(() -> validator.validateRequiredCommonVariables(
                Set.of(MailScenarioVariable.builder().key("RECRUIT_ALERT_APPLY_URL").label("URL").inputType(VariableInputType.URL).required(true).build()),
                Map.of("RECRUIT_ALERT_APPLY_URL", "https://ject.kr", "NAME", "젝트")
        )).doesNotThrowAnyException();
    }
}
