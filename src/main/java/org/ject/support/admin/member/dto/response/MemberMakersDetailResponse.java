package org.ject.support.admin.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberMakersDetailResponse(
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	Long memberActivityId,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String name,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	String email,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String phoneNumber,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	JobFamily jobFamily,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	CareerDetails careerDetails,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	MakersTeam makersTeam,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	RecruitTypeDetail recruitTypeDetail,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	Region region,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	List<String> interestedDomains,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	ExperiencePeriod experiencePeriod,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	Availability mentoringAvailability,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	Availability projectSupplementAvailability,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	Availability speakerAvailability,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	CareerLevel careerLevel,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	ActivityStatus activityStatus,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String skills,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String company,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String expertTopics,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String activityCertNumber,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String memo
) {
	public static MemberMakersDetailResponse from(MemberMakersDetailProjection projection) {
		return new MemberMakersDetailResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.email(),
			projection.phoneNumber(),
			projection.jobFamily(),
			projection.careerDetails(),
			projection.makersTeam(),
			projection.recruitTypeDetail(),
			projection.region(),
			projection.interestedDomains(),
			projection.experiencePeriod(),
			projection.mentoringAvailability(),
			projection.projectSupplementAvailability(),
			projection.speakerAvailability(),
			projection.careerLevel(),
			projection.activityStatus(),
			projection.skills(),
			projection.company(),
			projection.expertTopics(),
			projection.activityCertNumber(),
			projection.memo()
		);
	}
}
