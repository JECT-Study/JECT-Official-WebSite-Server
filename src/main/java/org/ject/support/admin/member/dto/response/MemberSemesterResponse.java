package org.ject.support.admin.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;

import org.ject.support.admin.member.dto.projection.MemberSemesterProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.ParticipationStatus;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.EventParticipation;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.domain.SemesterEventType;

public record MemberSemesterResponse(
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	Long memberActivityId,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String name,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	String email,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String phoneNumber,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	Region region,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	List<String> interestedDomains,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	JobFamily jobFamily,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	RecruitTypeDetail recruitTypeDetail,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	CareerDetails careerDetails,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	ActivityStatus activityStatus,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	ExperiencePeriod experiencePeriod,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String memo,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	Semester semester,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	Team team,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String certNumber,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String firstReview,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String secondReview,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	List<EventParticipationResponse> events,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	List<EventParticipationResponse> surveys
) {
	public static MemberSemesterResponse of(MemberSemesterProjection projection, List<SemesterEvent> semesterEvents,
		List<EventParticipation> eventParticipations) {
		Map<Long, ParticipationStatus> participationStatusByEventId = eventParticipations.stream()
			.collect(toMap(EventParticipation::getSemesterEventId, EventParticipation::getParticipationStatus));

		return new MemberSemesterResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.email(),
			projection.phoneNumber(),
			projection.region(),
			projection.interestedDomains(),
			projection.jobFamily(),
			projection.recruitTypeDetail(),
			projection.careerDetails(),
			projection.activityStatus(),
			projection.experiencePeriod(),
			projection.memo(),
			new Semester(projection.semesterId(), projection.semesterName()),
			toTeam(projection),
			projection.certNumber(),
			projection.firstReview(),
			projection.secondReview(),
			toEventParticipationResponses(semesterEvents, SemesterEventType.EVENT, participationStatusByEventId),
			toEventParticipationResponses(semesterEvents, SemesterEventType.SURVEY, participationStatusByEventId)
		);
	}

	private static Team toTeam(MemberSemesterProjection projection) {
		return projection.teamId() == null ? null : new Team(projection.teamId(), projection.teamName());
	}

	private static List<EventParticipationResponse> toEventParticipationResponses(List<SemesterEvent> semesterEvents,
		SemesterEventType type, Map<Long, ParticipationStatus> participationStatusByEventId) {
		return semesterEvents.stream()
			.filter(semesterEvent -> semesterEvent.getType() == type)
			.map(semesterEvent -> EventParticipationResponse.of(
				semesterEvent,
				participationStatusByEventId.get(semesterEvent.getId())
			))
			.toList();
	}

	public record Semester(
		@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
		Long semesterId,
		@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
		String semesterName
	) {
	}

	public record Team(
		@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
		Long teamId,
		@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
		String teamName
	) {
	}
}
