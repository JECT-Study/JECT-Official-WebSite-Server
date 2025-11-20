package org.ject.support.admin.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern.Flag;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Semester;

public record MemberRegisterRequest(
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
        @NotNull(message = "기수는 필수입니다.")
        @Schema(description = "기수 이름", example = "1기")
        @Pattern(regexp = "^\\d+기$", message = "숫자+'기' 형식으로 입력해주세요", flags = Flag.CANON_EQ)
        String semesterName
) {
    public Member toEntity(Semester semester) {
        return Member.builder()
                .role(this.role)
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .jobFamily(this.jobFamily)
                .semesterId(semester.getId())
                .build();
    }
}
