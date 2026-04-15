package org.ject.support.admin.mail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시나리오별로 운영자가 직접 정의하여 사용하는 커스텀 변수 정보입니다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "key") // 변수명 기준으로 동등성 확인
public class MailScenarioVariable {

    @Column(name = "variable_key", nullable = false)
    private String key;

    @Column(name = "variable_label", nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false)
    private VariableInputType inputType;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "description")
    private String description;

    @Builder
    public MailScenarioVariable(String key, String label, VariableInputType inputType, boolean required, String description) {
        this.key = key;
        this.label = label;
        this.inputType = inputType;
        this.required = required;
        this.description = description;
    }
}
