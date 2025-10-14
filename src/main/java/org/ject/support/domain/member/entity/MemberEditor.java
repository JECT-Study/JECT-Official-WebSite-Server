package org.ject.support.domain.member.entity;

import java.util.List;
import lombok.Builder;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;

@Builder
public record MemberEditor (
        String name,
        String phoneNumber,
        String email,
        Long semesterId,
        JobFamily jobFamily,
        ExperiencePeriod experiencePeriod,
        CareerDetails careerDetails,
        List<String> interestedDomains,
        Role role
) {
}