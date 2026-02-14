package org.ject.support.domain.member.entity;

import lombok.Builder;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;

import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;

import java.util.List;

@Builder
public record MemberEditor(
        String name,
        String phoneNumber,
        String email,

        Region region,
        ExperiencePeriod experiencePeriod,
        CareerDetails careerDetails,
        List<String> interestedDomains,
        Role role) {
}
