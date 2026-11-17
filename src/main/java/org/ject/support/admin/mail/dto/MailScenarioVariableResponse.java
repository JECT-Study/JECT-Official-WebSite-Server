package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 시나리오별 변수 목록 조회 응답 DTO입니다.
 */
public record MailScenarioVariableResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long scenarioId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<CustomVariableResponse> customVariables,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> personalVariables
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
    ) {
    }
}
