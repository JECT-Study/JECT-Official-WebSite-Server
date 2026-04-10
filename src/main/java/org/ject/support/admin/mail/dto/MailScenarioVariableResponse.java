package org.ject.support.admin.mail.dto;

import java.util.List;

/**
 * 시나리오별 변수 목록 조회 응답 DTO입니다.
 */
public record MailScenarioVariableResponse(
        Long scenarioId,
        String name,
        List<VariableResponse> commonVariables,
        List<VariableResponse> personalVariables
) {
    /**
     * 변수 키/라벨 표현 DTO입니다.
     */
    public record VariableResponse(
            String key,
            String label
    ) {
    }
}
