package org.ject.support.admin.member.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record MemberMakersListRequest(
	@Positive
	Long cursor,

	// 정책 변경에 대비해 여유롭게 설정
	@Min(1)
	@Max(100)
	Integer size
) {
	//size가 없으면 기본 30개
	public int getSizeOrDefault() {
		return size == null ? 30 : size;
	}
}
