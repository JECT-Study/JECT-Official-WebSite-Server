package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.service.AdminMemberMakersUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/members/makers")
@RequiredArgsConstructor
public class AdminMemberMakersController implements AdminMemberMakersApiSpec{

	private final AdminMemberMakersUseCase adminMemberMakersUseCase;

	// 메이커스팀 구성원 추가
	@Override
	@PostMapping
	public void createAdminMemberMakers(
		@RequestBody @Valid CreateMemberMakersRequest request) {
		adminMemberMakersUseCase.createMemberMakers(request);
	}
}
