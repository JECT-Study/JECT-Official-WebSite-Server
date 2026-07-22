package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "AdminMemberMakers", description = "메이커스팀 구성원 API")
public interface AdminMemberMakersApiSpec {
	@Operation(
		summary = "메이커스팀 구성원 추가",
		description = "메이커스팀 구성원을 추가합니다."
	)
	void createAdminMemberMakers(@RequestBody @Valid CreateMemberMakersRequest request);
}
