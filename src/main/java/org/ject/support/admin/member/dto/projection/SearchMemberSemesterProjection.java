package org.ject.support.admin.member.dto.projection;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;

public record SearchMemberSemesterProjection(
	Long memberActivityId,
	String name,
	JobFamily jobFamily,
	String phoneNumber,
	CareerDetails careerDetails,
	ExperiencePeriod experiencePeriod,
	ActivityStatus status
) {
}
