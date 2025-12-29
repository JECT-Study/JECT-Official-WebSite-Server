package org.ject.support.domain.member.dto;

import lombok.Builder;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.Member;

import java.util.List;

@Builder
public record MemberProfileResponse(
        Long id,
        String name,
        String phoneNumber,
        CareerDetails careerDetails,
        Region region,
        ExperiencePeriod experiencePeriod,
        List<String> interestedDomains
) {
    public static MemberProfileResponse of(Member member) {
        return MemberProfileResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .careerDetails(member.getCareerDetails())
                .region(member.getRegion())
                .experiencePeriod(member.getExperiencePeriod())
                .interestedDomains(member.getInterestedDomains())
                .build();
    }
}
