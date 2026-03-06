package org.ject.support.domain.member.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.ject.support.domain.member.JobFamily;

public record MemberProjection(
        Long id,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String semesterName
) {
    @QueryProjection
    public MemberProjection(
            Long id,
            String name,
            String phoneNumber,
            String email,
            JobFamily jobFamily,
            String semesterName
    ) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.jobFamily = jobFamily;
        this.semesterName = semesterName;
    }
}
