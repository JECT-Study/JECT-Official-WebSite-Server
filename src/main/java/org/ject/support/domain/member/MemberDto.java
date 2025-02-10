package org.ject.support.domain.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class MemberDto {

    @Data
    @Builder
    @AllArgsConstructor
    public static class TempMemberJoinRequest {

        @NotNull
        @Size(max = 30)
        @Email(regexp = "^[a-zA-Z0-9._%+-]{1,20}@[a-zA-Z0-9.-]{1,10}\\.[a-zA-Z]{2,}$")
        private String email;

        public static Member toEntity(String email) {
            return Member.builder()
                    .name("UNKNOWN")
                    .phoneNumber(generateRandomPhoneNumber())
                    .email(email)
                    .role(Role.TEMP)
                    .build();
        }

        private static String generateRandomPhoneNumber() {
            return "000" + (int) (Math.random() * 100000000);
        }
    }
}
