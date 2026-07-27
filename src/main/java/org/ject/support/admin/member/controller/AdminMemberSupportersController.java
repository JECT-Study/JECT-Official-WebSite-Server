package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.service.AdminMemberSupportersUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/members/supporters")
@RequiredArgsConstructor
public class AdminMemberSupportersController implements AdminMemberSupportersApiSpec {

	private final AdminMemberSupportersUseCase adminMemberSupportersUseCase;

	// 운영 서포터즈 구성원 추가
	@Override
	@PostMapping
	public void createAdminMemberSupporters(
		@RequestBody @Valid CreateMemberSupportersRequest request) {
		adminMemberSupportersUseCase.createMemberSupporters(request);
	}
}
