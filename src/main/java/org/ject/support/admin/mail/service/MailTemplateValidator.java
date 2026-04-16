package org.ject.support.admin.mail.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.springframework.stereotype.Component;

@Component
public class MailTemplateValidator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");
    private static final Pattern PLACEHOLDER_KEY_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

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

        Set<String> allowedKeys = new HashSet<>();
        for (ReservedMailVariable rv : ReservedMailVariable.values()) {
            allowedKeys.add(rv.name());
        }
        for (MailScenarioVariable cv : safeCustomVariables) {
            allowedKeys.add(cv.getKey());
        }

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

    public void validateRequiredCommonVariables(Set<MailScenarioVariable> customVariables, Map<String, ?> variables) {
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

    private Set<String> extractPlaceholderKeys(String template) {
        Set<String> keys = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }
}
