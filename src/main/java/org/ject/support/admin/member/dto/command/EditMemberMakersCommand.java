package org.ject.support.admin.member.dto.command;

import org.ject.support.admin.member.dto.request.UpdateMemberMakersRequest;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.MakersTeam;

public record EditMemberMakersCommand(
	MakersTeam makersTeam,
	Availability mentoringAvailability,
	Availability projectSupplementAvailability,
	Availability speakerAvailability,
	CareerLevel careerLevel,
	String skills,
	String company,
	String expertTopics,
	String activityCertNumber
) {
	public static EditMemberMakersCommand from(UpdateMemberMakersRequest request) {
		return new EditMemberMakersCommand(
			request.makersTeam(),
			request.mentoringAvailability(),
			request.projectSupplementAvailability(),
			request.speakerAvailability(),
			request.careerLevel(),
			request.skills(),
			request.company(),
			request.expertTopics(),
			request.activityCertNumber()
		);
	}
}
