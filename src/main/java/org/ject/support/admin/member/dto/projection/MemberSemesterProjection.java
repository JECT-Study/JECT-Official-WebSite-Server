package org.ject.support.admin.member.dto.projection;

import java.util.List;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberSemesterProjection(
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
	Long semesterId,
	String semesterName,
	Long teamId,
	String teamName,
	String certNumber,
	String firstReview,
	String secondReview
) {
}
