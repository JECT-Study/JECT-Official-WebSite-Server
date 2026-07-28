package org.ject.support.admin.member.dto.projection;

import java.util.List;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberMakersDetailProjection(
	Long memberActivityId,

	String name,

	String email,

	String phoneNumber,

	JobFamily jobFamily,

	CareerDetails careerDetails,

	MakersTeam makersTeam,

	RecruitTypeDetail recruitTypeDetail,

	Region region,

	List<String> interestedDomains,

	ExperiencePeriod experiencePeriod,

	Availability mentoringAvailability,

	Availability projectSupplementAvailability,

	Availability speakerAvailability,

	CareerLevel careerLevel,

	ActivityStatus activityStatus,

	String skills,

	String company,

	String expertTopics,

	String activityCertNumber,

	String memo

) {
}
