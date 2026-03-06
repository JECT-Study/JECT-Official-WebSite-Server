package org.ject.support.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminAuthSendRequest(
        @NotBlank(message = "Email은 필수 입력 값입니다.")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        String email
) {
}
