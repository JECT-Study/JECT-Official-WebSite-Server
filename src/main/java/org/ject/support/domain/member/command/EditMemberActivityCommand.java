package org.ject.support.domain.member.command;

import org.ject.support.admin.member.dto.request.UpdateMemberMakersRequest;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record EditMemberActivityCommand(
	JobFamily jobFamily,
	CareerDetails careerDetails,
	RecruitTypeDetail recruitTypeDetail,
	ExperiencePeriod experiencePeriod,
	String memo
) {
	public static EditMemberActivityCommand from(UpdateMemberMakersRequest request) {
		return new EditMemberActivityCommand(
			request.jobFamily(),
			request.careerDetails(),
			request.recruitTypeDetail(),
			request.experiencePeriod(),
			request.memo()
		);
	}
}
