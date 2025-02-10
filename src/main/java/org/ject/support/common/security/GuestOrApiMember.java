package org.ject.support.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 회원/비회원 모두 접근 가능한 API에서 사용하는 인증 정보
 */
@Data
@Getter
@AllArgsConstructor
public class GuestOrApiMember {

    private Long memberId;

    public boolean isGuest() {
        return memberId.equals(-1L);
    }
}
