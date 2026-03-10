package org.ject.support.admin.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminLoginRequest(
        @NotBlank(message = "Email은 필수 입력 값입니다.")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        String email,

        @NotBlank(message = "PIN은 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{6}$", message = "PIN 번호는 영문 대소문자와 숫자를 포함한 6자리여야 합니다.")
        String pin
) {
}
