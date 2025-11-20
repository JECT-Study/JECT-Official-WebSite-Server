package org.ject.support.admin.member.dto;

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
    public MemberResponse(
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
        if (semesterName != null) {
            this.semesterName = semesterName.replaceAll("\\D", "");
        } else {
            this.semesterName = "";
        }
    }
}
