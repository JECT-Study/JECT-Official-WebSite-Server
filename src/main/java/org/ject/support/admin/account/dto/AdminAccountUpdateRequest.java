package org.ject.support.admin.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.ject.support.domain.member.Role;

@Schema(description = "관리자 계정 정보 수정 요청")
public record AdminAccountUpdateRequest(
        @Schema(description = "관리자 계정 이메일", example = "admin@ject.kr", maxLength = 30)
        @NotBlank
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 30, message = "이메일 길이는 최대 30자리 까지 가능합니다.")
        String email,

        @Schema(description = "관리자 이름", example = "김젝트", maxLength = 20, nullable = true)
        @Size(max = 20, message = "이름 길이는 최대 20자리 까지 가능합니다.")
        String name,

        @Schema(
                description = "관리자 계정 유형",
                example = "OPERATIONS",
                allowableValues = {"ADMIN", "OPERATIONS", "SUPPORTER"})
        @NotNull
        Role role,

        @Schema(description = "관리자 계정 활성화 여부", example = "true")
        @NotNull
        Boolean active
) {

    public String normalizeName() {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name;
    }
}
