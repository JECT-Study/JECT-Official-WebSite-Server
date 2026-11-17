package org.ject.support.admin.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record MemberSupportersDetailResponse(
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	Long memberActivityId,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String name,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String phoneNumber,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	String email,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	MemberType memberType,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
	JobFamily jobFamily,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	RecruitTypeDetail recruitTypeDetail,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	ActivityStatus activityStatus,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	LocalDate startDate,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	LocalDate endDate,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	String activityCertNumber,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
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
