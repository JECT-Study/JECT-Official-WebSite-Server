package org.ject.support.admin.member.dto.response;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;

public record SearchMemberSemesterResponse(
	Long memberActivityId,
	String name,
	JobFamily jobFamily,
	String phoneNumber,
	CareerDetails careerDetails,
	ExperiencePeriod experiencePeriod,
	ActivityStatus status
) {
	public static SearchMemberSemesterResponse from(SearchMemberSemesterProjection projection) {
		return new SearchMemberSemesterResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.jobFamily(),
			projection.phoneNumber(),
			projection.careerDetails(),
			projection.experiencePeriod(),
			projection.status()
		);
	}
}
