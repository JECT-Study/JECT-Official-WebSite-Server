package org.ject.support.admin.mail.dto;

import java.util.List;

/**
 * 시나리오별 변수 목록 조회 응답 DTO입니다.
 */
public record MailScenarioVariableResponse(
        Long scenarioId,
        String name,
        List<CustomVariableResponse> customVariables,
        List<String> personalVariables
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
    ) {
    }
}
