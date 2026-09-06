package org.ject.support.admin.member.dto.request;

import org.ject.support.domain.member.ParticipationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일반 구성원 행사 참여 상태 수정 요청")
public record EditEventParticipationRequest(

	@Schema(description = "기수별 행사 ID", example = "1")
	@NotNull(message = "기수별 행사를 선택해주세요.")
	Long semesterEventId,

	@Schema(description = "행사 참여 상태. null이면 미지정 처리", example = "ATTENDED",
		allowableValues = {"ATTENDED", "ABSENT"}, nullable = true)
	ParticipationStatus participationStatus
) {
}
