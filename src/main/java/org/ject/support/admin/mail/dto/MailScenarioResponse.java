package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.MailVariable;

/**
 * 메일 시나리오 조회 응답 DTO입니다.
 */
public record MailScenarioResponse(
        Long id,
        String name,
        MailScenarioCategory category,
        MailScenarioType type,
        String scenarioCode,
        String subjectTemplate,
        String bodyTemplate,
        boolean active,
        LocalDateTime createdAt,
        List<VariableResponse> commonVariables,
        List<VariableResponse> personalVariables
) {
    /**
     * 시나리오 변수 표현 DTO입니다.
     */
    public record VariableResponse(String key, String label) {}

    public static MailScenarioResponse from(MailScenario scenario) {
        List<VariableResponse> common = scenario.getVariables().stream()
                .filter(MailVariable::isCommon)
                .map(v -> new VariableResponse(v.name(), v.getLabel()))
                .toList();
        List<VariableResponse> personal = scenario.getVariables().stream()
                .filter(v -> !v.isCommon())
                .map(v -> new VariableResponse(v.name(), v.getLabel()))
                .toList();

        return new MailScenarioResponse(
                scenario.getId(),
                scenario.getName(),
                scenario.getCategory(),
                scenario.getType(),
                scenario.getScenarioCode(),
                scenario.getSubjectTemplate(),
                scenario.getBodyTemplate(),
                scenario.isActive(),
                scenario.getCreatedAt(),
                common,
                personal
        );
    }
}
