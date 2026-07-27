package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "AdminMemberSupporters", description = "운영 서포터즈 구성원 API")
public interface AdminMemberSupportersApiSpec {

	@Operation(
		summary = "운영 서포터즈 구성원 추가",
		description = "운영 서포터즈 구성원을 추가합니다."
	)
	void createAdminMemberSupporters(@RequestBody @Valid CreateMemberSupportersRequest request);
}
