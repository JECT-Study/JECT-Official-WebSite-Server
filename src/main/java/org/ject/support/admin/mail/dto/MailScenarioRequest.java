package org.ject.support.admin.mail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.MailVariable;

/**
 * 메일 시나리오 생성/수정 요청 DTO입니다.
 */
public record MailScenarioRequest(
        @NotBlank(message = "시나리오 이름은 필수입니다.")
        String name,
        @NotNull(message = "시나리오 카테고리는 필수입니다.")
        MailScenarioCategory category,
        @NotNull(message = "시나리오 타입은 필수입니다.")
        MailScenarioType type,
        @NotBlank(message = "시나리오 코드는 필수입니다.")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "시나리오 코드는 대문자/숫자/언더스코어만 허용합니다.")
        String scenarioCode,
        @NotBlank(message = "메일 제목 템플릿은 필수입니다.")
        String subjectTemplate,
        @NotBlank(message = "메일 본문 템플릿은 필수입니다.")
        String bodyTemplate,
        @NotNull(message = "활성화 여부는 필수입니다.")
        Boolean active,
        @NotNull(message = "변수 리스트는 null일 수 없습니다.")
        Set<MailVariable> variables
) {
}
