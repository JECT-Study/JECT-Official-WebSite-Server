package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;

/**
 * 메일 시나리오 조회 응답 DTO입니다.
 */
public record MailScenarioResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        MailScenarioCategory category,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        MailScenarioType type,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String scenarioCode,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String subjectTemplate,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String bodyTemplate,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean active,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<CustomVariableResponse> customVariables
) {
    /**
     * 커스텀 변수 표현 DTO입니다.
     */
    public record CustomVariableResponse(
            @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
            String key,
            @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
            String label,
            @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
            String inputType,
            @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
            boolean required,
            @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
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
