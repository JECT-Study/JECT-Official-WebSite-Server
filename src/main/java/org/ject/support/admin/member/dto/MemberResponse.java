package org.ject.support.admin.member.dto;

import lombok.Builder;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.dto.MemberProjection;

@Builder
public record MemberResponse(
        Long id,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        String semesterName
) {
    public static MemberResponse from(MemberProjection projection) {
        String semester = projection.semesterName() != null
                ? projection.semesterName().replaceAll("\\D", "")
                : "";
        return new MemberResponse(
                projection.id(),
                projection.name(),
                projection.phoneNumber(),
                projection.email(),
                projection.jobFamily(),
                semester
        );
    }
}

