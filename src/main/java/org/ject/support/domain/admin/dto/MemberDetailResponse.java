package org.ject.support.domain.admin.dto;

import lombok.Builder;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Semester;

@Builder
public record MemberDetailResponse(
        Long id,
        Role role,
        String name,
        String phoneNumber,
        String email,
        JobFamily jobFamily,
        Region region,
        String semesterName
) {
    public static MemberDetailResponse toResponse(
            Member member,
            Semester semester,
            JobFamily jobFamily
    ) {
        return MemberDetailResponse.builder()
                .id(member.getId())
                .role(member.getRole())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .jobFamily(jobFamily)
                .region(member.getRegion())
                .semesterName(semester.getName().replaceAll("\\D", ""))
                .build();
    }
}
