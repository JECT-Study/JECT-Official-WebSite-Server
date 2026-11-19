package org.ject.support.admin.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.member.ParticipationStatus;
import org.ject.support.domain.recruit.domain.SemesterEvent;

public record EventParticipationResponse(
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	Long semesterEventId,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	String name,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
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
