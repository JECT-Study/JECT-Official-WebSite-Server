package org.ject.support.admin.member.dto.response;

import java.time.LocalDate;

import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberSupportersDetailResponse(
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
	public static MemberSupportersDetailResponse from(MemberSupportersDetailProjection projection) {
		return new MemberSupportersDetailResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.phoneNumber(),
			projection.email(),
			projection.memberType(),
			projection.jobFamily(),
			projection.recruitTypeDetail(),
			projection.activityStatus(),
			projection.startDate(),
			projection.endDate(),
			projection.activityCertNumber(),
			projection.memo()
		);
	}
}
