package org.ject.support.admin.member.dto.request;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "구성원 일괄 삭제 요청")
public record DeleteMembersRequest(
	@Schema(description = "삭제할 구성원 활동 ID 목록", example = "[1, 2, 3]")
	@NotEmpty Set<@NotNull Long> memberActivityIds
) {
}
