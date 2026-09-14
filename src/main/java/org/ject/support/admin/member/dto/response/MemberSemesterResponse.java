package org.ject.support.admin.member.dto.response;

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
	Long memberActivityId,
	String name,
	String email,
	String phoneNumber,
	Region region,
	List<String> interestedDomains,
	JobFamily jobFamily,
	RecruitTypeDetail recruitTypeDetail,
	CareerDetails careerDetails,
	ActivityStatus activityStatus,
	ExperiencePeriod experiencePeriod,
	String memo,
	Semester semester,
	Team team,
	String certNumber,
	String firstReview,
	String secondReview,
	List<EventParticipationResponse> events,
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

	public record Semester(Long semesterId, String semesterName) {
	}

	public record Team(Long teamId, String teamName) {
	}
}
