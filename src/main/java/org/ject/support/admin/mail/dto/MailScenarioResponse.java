package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;

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
        List<CustomVariableResponse> customVariables
) {
    /**
     * 커스텀 변수 표현 DTO입니다.
     */
    public record CustomVariableResponse(
            String key, 
            String label, 
            String inputType, 
            boolean required, 
            String description
    ) {}

    public static MailScenarioResponse from(MailScenario scenario) {
        List<CustomVariableResponse> customVars = scenario.getCustomVariables().stream()
                .map(v -> new CustomVariableResponse(
                        v.getKey(), 
                        v.getLabel(), 
                        v.getInputType().name(), 
                        v.isRequired(), 
                        v.getDescription()))
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
                customVars
        );
    }
}
