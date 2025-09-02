package org.ject.support.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;

public class MemberDto {

    public record RegisterRequest (

            @NotBlank @Pattern(regexp = "^\\d{6}$", message = "PIN 번호는 6자리 숫자여야 합니다.") String pin) {

        public Member toEntity(String email, String encodedPin, Long semesterId) {
            return Member.builder()
                    .email(email)
                    .semesterId(semesterId)
                    .pin(encodedPin)
                    .status(MemberStatus.ACTIVE)
                    .role(Role.TEMP)
                    .build();
        }
    }

    /**
     * 임시회원이 최초로 이름과 전화번호를 등록할 때 사용하는 DTO
     */
    public record InitialProfileRequest (

            @NotBlank @Pattern(regexp = "^[가-힣]{1,5}$", message = "한글 1~5글자만 입력 가능합니다.")
            String name,

            @NotBlank @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
            String phoneNumber) {

    }

    public record UpdatePinRequest(
            @NotBlank @Pattern(regexp = "^\\d{6}$", message = "PIN 번호는 6자리 숫자여야 합니다.") String pin) {
    }
}
