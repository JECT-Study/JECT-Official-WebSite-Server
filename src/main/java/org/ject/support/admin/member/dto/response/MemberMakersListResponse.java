package org.ject.support.admin.member.dto.response;

import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberMakersListResponse(
	Long memberActivityId,
	String name,
	String email,
	String phoneNumber,
	JobFamily jobFamily,
	MakersTeam makersTeam,
	RecruitTypeDetail recruitTypeDetail,
	ActivityStatus activityStatus,
	String memo
) {
	public static MemberMakersListResponse from(MemberMakersListProjection projection) {
		return new MemberMakersListResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.email(),
			projection.phoneNumber(),
			projection.jobFamily(),
			projection.makersTeam(),
			projection.recruitTypeDetail(),
			projection.activityStatus(),
			projection.memo()
		);
	}
}
