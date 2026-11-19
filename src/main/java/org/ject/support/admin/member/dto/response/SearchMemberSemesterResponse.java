package org.ject.support.admin.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;

public record SearchMemberSemesterResponse(
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	Long memberActivityId,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String name,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	JobFamily jobFamily,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String phoneNumber,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	CareerDetails careerDetails,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	ExperiencePeriod experiencePeriod,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	ActivityStatus status
) {
	public static SearchMemberSemesterResponse from(SearchMemberSemesterProjection projection) {
		return new SearchMemberSemesterResponse(
			projection.memberActivityId(),
			projection.name(),
			projection.jobFamily(),
			projection.phoneNumber(),
			projection.careerDetails(),
			projection.experiencePeriod(),
			projection.status()
		);
	}
}
