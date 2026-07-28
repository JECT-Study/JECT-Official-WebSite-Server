package org.ject.support.admin.member.dto.response;

import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberSupportersListResponse(
	Long memberActivityId,
	String name,
	String phoneNumber,
	JobFamily jobFamily,
	RecruitTypeDetail recruitTypeDetail,
	ActivityStatus activityStatus,
	String memo
) {
	public static MemberSupportersListResponse from(MemberSupportersListProjection projection) {
		return new MemberSupportersListResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.phoneNumber(),
			projection.jobFamily(),
			projection.recruitTypeDetail(),
			projection.activityStatus(),
			projection.memo()
		);
	}
}
