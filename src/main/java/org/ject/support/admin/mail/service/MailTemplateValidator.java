package org.ject.support.admin.mail.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ject.support.admin.mail.domain.MailVariable;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.springframework.stereotype.Component;

@Component
public class MailTemplateValidator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");
    private static final Pattern PLACEHOLDER_KEY_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    /**
     * `${KEY}` 형태의 기본 문법이 올바른지 검증합니다.
     */
    public void validateSyntax(String template) {
        // 1. null 템플릿은 문법 오류로 처리합니다.
        if (template == null) {
            throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
        }

        // 2. `${` 위치를 순회하며 짝이 맞는 `}` 와 키 규칙을 검증합니다.
        int startIndex = 0;
        while (true) {
            int open = template.indexOf("${", startIndex);
            if (open < 0) {
                // 3. 더 이상 플레이스홀더 시작 토큰이 없으면 검증을 종료합니다.
                return;
            }

            int close = template.indexOf('}', open + 2);
            if (close < 0) {
                throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
            }

            String key = template.substring(open + 2, close);
            // 정규식 [A-Za-z0-9_]+ 검사로 빈 값, 괄호, 띄어쓰기 포함 여부가 모두 걸러집니다.
            if (!PLACEHOLDER_KEY_PATTERN.matcher(key).matches()) {
                throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
            }

            startIndex = close + 1;
        }
    }

    /**
     * 템플릿에 사용된 플레이스홀더가 허용된 변수 집합에 포함되는지 검증합니다.
     */
    public void validateAllowedPlaceholders(String template, Set<MailVariable> allowedVariables) {
        // 단독 호출 시에도 null 템플릿을 문법 오류로 일관 처리합니다.
        if (template == null) {
            throw new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX);
        }

        // 1. null 안전을 위해 허용 변수 집합을 기본값으로 정규화합니다.
        Set<MailVariable> safeAllowedVariables = allowedVariables != null ? allowedVariables : Set.of();

        // 2. Enum 이름 기준 허용 키 집합을 생성합니다.
        Set<String> allowedKeys = safeAllowedVariables.stream()
                .map(Enum::name)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        // 3. 템플릿에서 추출한 키가 허용 목록에 없으면 예외를 발생시킵니다.
        for (String placeholderKey : extractPlaceholderKeys(template)) {
            if (!allowedKeys.contains(placeholderKey)) {
                throw new MailException(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE);
            }
        }
    }

    /**
     * 공통 변수로 지정된 항목이 실제 요청 변수 맵에 모두 채워졌는지 검증합니다.
     */
    public void validateRequiredCommonVariables(Set<MailVariable> allowedVariables, Map<String, ?> variables) {
        // 1. null 안전을 위해 허용 변수/입력 변수 맵을 정규화합니다.
        Set<MailVariable> safeAllowedVariables = allowedVariables != null ? allowedVariables : Set.of();
        Map<String, ?> safeVariables = variables != null ? variables : Map.of();

        // 2. 공통 변수만 순회하며 누락/공백 값을 검증합니다.
        for (MailVariable variable : safeAllowedVariables) {
            if (!variable.isCommon()) {
                continue;
            }

            Object value = safeVariables.get(variable.name());
            if (value == null) {
                throw new MailException(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
            }

            if (value instanceof CharSequence charSequence && charSequence.toString().isBlank()) {
                throw new MailException(MailErrorCode.MISSING_REQUIRED_COMMON_VARIABLE);
            }
        }
    }

    private Set<String> extractPlaceholderKeys(String template) {
        // 1. 플레이스홀더 키를 담을 Set을 초기화합니다.
        Set<String> keys = new HashSet<>();
        // 2. 정규식을 이용해 `${KEY}` 패턴을 순회합니다.
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            // 3. KEY 부분만 추출해 중복 없이 누적합니다.
            keys.add(matcher.group(1));
        }
        // 4. 추출된 키 집합을 반환합니다.
        return keys;
    }
}
