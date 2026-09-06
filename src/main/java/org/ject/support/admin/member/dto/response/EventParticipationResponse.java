package org.ject.support.admin.member.dto.response;

import org.ject.support.domain.member.ParticipationStatus;
import org.ject.support.domain.recruit.domain.SemesterEvent;

public record EventParticipationResponse(
	Long semesterEventId,
	String name,
	ParticipationStatus participationStatus
) {
	public static EventParticipationResponse of(SemesterEvent semesterEvent, ParticipationStatus participationStatus) {
		return new EventParticipationResponse(
			semesterEvent.getId(),
			semesterEvent.getName(),
			participationStatus
		);
	}
}
