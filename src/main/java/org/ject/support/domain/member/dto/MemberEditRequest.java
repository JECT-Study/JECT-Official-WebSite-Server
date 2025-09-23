package org.ject.support.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;

@Builder
public record MemberEditRequest(
        @NotNull(message = "구분은 필수입니다.")
        Role role,
        @NotBlank @Pattern(regexp = "^[가-힣]{1,5}$", message = "한글 1~5글자만 입력 가능합니다.")
        String name,
        @NotBlank @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
        String phoneNumber,
        @NotBlank @Email
        String email,
        @NotNull(message = "포지션은 필수입니다.")
        JobFamily jobFamily,
        Long semesterId
) {
}
