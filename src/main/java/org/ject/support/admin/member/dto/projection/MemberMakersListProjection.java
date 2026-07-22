package org.ject.support.admin.member.dto.projection;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberMakersListProjection(
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
}
