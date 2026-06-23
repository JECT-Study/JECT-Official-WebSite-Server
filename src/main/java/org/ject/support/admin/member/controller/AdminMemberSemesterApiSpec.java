package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "AdminMemberSemester", description = "일반 구성원 API")
public interface AdminMemberSemesterApiSpec {
	@Operation(
		summary = "일반 구성원 추가",
		description = "일반 구성원을 추가합니다."
	)
	void createAdminMemberSemester(@RequestBody @Valid CreateMemberSemesterRequest request);
}
