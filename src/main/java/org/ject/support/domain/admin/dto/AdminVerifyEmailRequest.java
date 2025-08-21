package org.ject.support.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminVerifyEmailRequest(
        @NotBlank(message = "Email은 필수 입력 값입니다.")
        String email,

        @NotBlank(message = "Code는 필수 입력 값입니다.")
        @Size(min = 6, max = 6, message = "Code는 6자리여야 합니다.")
        String code
) {
}
