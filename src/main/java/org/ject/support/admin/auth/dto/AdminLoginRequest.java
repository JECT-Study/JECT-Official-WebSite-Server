package org.ject.support.admin.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 8자 이상 입력해주세요.")
        @Size(max = 64, message = "비밀번호는 64자 이하로 입력해주세요.")
        String password
) {
}
