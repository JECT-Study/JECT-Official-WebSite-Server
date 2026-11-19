package org.ject.support.admin.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberMakersListResponse(
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
	MakersTeam makersTeam,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	RecruitTypeDetail recruitTypeDetail,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	ActivityStatus activityStatus,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
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
