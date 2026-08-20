package org.ject.support.admin.mail.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.common.util.DateTimeUtil;
import org.springframework.stereotype.Component;

@Component
public class MailTemplateValidator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");
    private static final Pattern PLACEHOLDER_KEY_PATTERN = Pattern.compile("[A-Za-z0-9_]+");
    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("(?!0000-)\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");

    /**
     * ${KEY} 형태의 기본 문법이 올바른지 검증합니다.
     */
    public void validateSyntax(String template) {
        if (template == null) {
            throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
        }

        int startIndex = 0;
        while (true) {
            int open = template.indexOf("${", startIndex);
            if (open < 0) {
                return;
            }

            int close = template.indexOf('}', open + 2);
            if (close < 0) {
                throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
            }

            String key = template.substring(open + 2, close);
            if (!PLACEHOLDER_KEY_PATTERN.matcher(key).matches()) {
                throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
            }

            startIndex = close + 1;
        }
    }

    /**
     * 템플릿에 사용된 플레이스홀더가 예약된 변수이거나 커스텀 변수 집합에 포함되는지 검증합니다.
     */
    public void validateAllowedPlaceholders(String template, Set<MailScenarioVariable> customVariables) {
        if (template == null) {
            throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
        }

        Set<MailScenarioVariable> safeCustomVariables = customVariables != null ? customVariables : Set.of();

        Set<String> allowedKeys = getAllowedVariableKeys(safeCustomVariables);

        Set<String> usedKeys = extractPlaceholderKeys(template);
        Set<String> invalidKeys = usedKeys.stream()
                .filter(key -> !allowedKeys.contains(key))
                .collect(Collectors.toSet());

        if (!invalidKeys.isEmpty()) {
            String message = String.format("%s : [%s]", 
                MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE.getMessage(),
                String.join(", ", invalidKeys.stream().sorted().toList()));
            throw new MailException(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE, message);
        }
    }

    private void validateRequiredVariables(Set<MailScenarioVariable> customVariables, Map<String, ?> variables) {
        Set<MailScenarioVariable> safeCustomVariables = customVariables != null ? customVariables : Set.of();
        Map<String, ?> safeVariables = variables != null ? variables : Map.of();

        for (MailScenarioVariable variable : safeCustomVariables) {
            if (!variable.isRequired()) {
                continue;
            }

            Object value = safeVariables.get(variable.getKey());
            if (value == null) {
                throw new MailException(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
            }

            if (value instanceof CharSequence charSequence && charSequence.toString().isBlank()) {
                throw new MailException(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
            }
        }
    }

    /**
     * 미리보기와 발송에 사용할 입력 변수의 선언, 필수 여부, 타입을 검증합니다.
     */
    public void validateVariables(Set<MailScenarioVariable> customVariables, Map<String, ?> variables) {
        Set<MailScenarioVariable> safeCustomVariables = customVariables != null ? customVariables : Set.of();
        Map<String, ?> safeVariables = variables != null ? variables : Map.of();

        validateRequiredVariables(safeCustomVariables, safeVariables);
        validateDeclaredVariableKeys(safeCustomVariables, safeVariables);

        for (MailScenarioVariable variable : safeCustomVariables) {
            Object value = safeVariables.get(variable.getKey());
            if (isBlank(value)) {
                continue;
            }

            if (variable.getInputType() == VariableInputType.DATE_TIME) {
                validateDateTime(variable.getKey(), value);
            }
        }
    }

    private void validateDeclaredVariableKeys(Set<MailScenarioVariable> customVariables, Map<String, ?> variables) {
        Set<String> allowedKeys = getAllowedVariableKeys(customVariables);
        Set<String> invalidKeys = variables.keySet().stream()
                .filter(key -> !allowedKeys.contains(key))
                .map(key -> key == null ? "null" : key)
                .collect(Collectors.toSet());

        if (!invalidKeys.isEmpty()) {
            String message = String.format("%s : [%s]",
                    MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE.getMessage(),
                    String.join(", ", invalidKeys.stream().sorted().toList()));
            throw new MailException(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE, message);
        }
    }

    private void validateDateTime(String key, Object value) {
        if (!(value instanceof CharSequence charSequence)
                || !DATE_TIME_PATTERN.matcher(charSequence).matches()) {
            throwInvalidVariableValue(key);
            return;
        }

        try {
            LocalDateTime.parse(charSequence, DateTimeUtil.DEFAULT_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throwInvalidVariableValue(key);
        }
    }

    private void throwInvalidVariableValue(String key) {
        String message = String.format("%s : [%s]", MailErrorCode.INVALID_VARIABLE_VALUE.getMessage(), key);
        throw new MailException(MailErrorCode.INVALID_VARIABLE_VALUE, message);
    }

    private boolean isBlank(Object value) {
        return value == null || value instanceof CharSequence charSequence && charSequence.toString().isBlank();
    }

    private Set<String> getAllowedVariableKeys(Set<MailScenarioVariable> customVariables) {
        Set<String> allowedKeys = new HashSet<>();
        for (ReservedMailVariable rv : ReservedMailVariable.values()) {
            allowedKeys.add(rv.name());
        }
        for (MailScenarioVariable cv : customVariables) {
            allowedKeys.add(cv.getKey());
        }
        return allowedKeys;
    }

    private Set<String> extractPlaceholderKeys(String template) {
        Set<String> keys = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }
}
