package org.ject.support.domain.member.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.ject.support.domain.member.JobFamily;

@Builder
public record MemberResponse(
        Long id,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String semesterName
) {
    @QueryProjection
    public MemberResponse {
        // 필요에 따라 추가 검증 로직 구현
    }
}
