package org.ject.support.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminAuthSendEmailRequest(
        @NotBlank(message = "Email은 필수 입력 값입니다.")
        String email
) {
}
