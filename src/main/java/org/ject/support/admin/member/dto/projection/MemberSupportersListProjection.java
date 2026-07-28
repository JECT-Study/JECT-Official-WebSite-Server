package org.ject.support.admin.member.dto.projection;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberSupportersListProjection(
	Long memberActivityId,
	String name,
	String phoneNumber,
	JobFamily jobFamily,
	RecruitTypeDetail recruitTypeDetail,
	ActivityStatus activityStatus,
	String memo
) {
}
