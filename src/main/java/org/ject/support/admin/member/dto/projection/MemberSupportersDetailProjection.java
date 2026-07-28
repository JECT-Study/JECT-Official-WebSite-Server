package org.ject.support.admin.member.dto.projection;

import java.time.LocalDate;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberSupportersDetailProjection(
	Long memberActivityId,
	String name,
	String phoneNumber,
	String email,
	MemberType memberType,
	JobFamily jobFamily,
	RecruitTypeDetail recruitTypeDetail,
	ActivityStatus activityStatus,
	LocalDate startDate,
	LocalDate endDate,
	String activityCertNumber,
	String memo
) {
}
