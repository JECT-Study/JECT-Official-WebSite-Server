package org.ject.support.admin.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Role;

@Schema(description = "관리자 계정 생성 요청")
public record AdminAccountCreateRequest(
        @Schema(description = "관리자 계정 이메일", example = "admin@ject.kr", maxLength = 30)
        @NotBlank
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 30, message = "이메일 길이는 최대 30자리 까지 가능합니다.")
        String email,

        @Schema(description = "관리자 계정 비밀번호", example = "password123", minLength = 8, maxLength = 64)
        @NotBlank
        @Size(min = 8, max = 64)
        String password,

        @Schema(description = "관리자 이름", example = "김젝트", maxLength = 20, nullable = true)
        @Size(max = 20, message = "이름 길이는 최대 20자리 까지 가능합니다.")
        String name,

        @Schema(
                description = "관리자 계정 유형",
                example = "OPERATIONS",
                allowableValues = {"ADMIN", "OPERATIONS", "SUPPORTER"})
        @NotNull
        Role role
) {

    private static final long DEFAULT_ADMIN_ACCOUNT_SEMESTER_ID = 1L;

    public Applicant toEntity(final String encodedPassword) {
        return Applicant.builder()
                .email(email)
                .pin(encodedPassword)
                .name(normalizeName())
                .role(role)
                .memberType(MemberType.fromRole(role))
                .semesterId(DEFAULT_ADMIN_ACCOUNT_SEMESTER_ID)
                .build();
    }

    private String normalizeName() {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name;
    }
}
