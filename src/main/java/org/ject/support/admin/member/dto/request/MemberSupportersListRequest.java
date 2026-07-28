package org.ject.support.admin.member.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record MemberSupportersListRequest(
	@Positive
	Long cursor,

	// 정책 변경에 대비해 여유롭게 설정
	@Min(value = 1, message = "조회 개수는 1개 이상이어야 합니다.")
	@Max(value = 100, message = "조회 개수는 100개 이하여야 합니다.")
	Integer size
) {
	// size가 없으면 기본 30개
	public int getSizeOrDefault() {
		return size == null ? 30 : size;
	}
}
