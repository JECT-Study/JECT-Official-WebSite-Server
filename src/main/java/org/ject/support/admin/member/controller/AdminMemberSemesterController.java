package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.service.AdminMemberUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/members/semester")
@RequiredArgsConstructor
public class AdminMemberSemesterController implements AdminMemberSemesterApiSpec {

	private final AdminMemberUseCase adminMemberUsecase;

	//일반 구성원 추가
	@Override
	@PostMapping
	public void createAdminMemberSemester(CreateMemberSemesterRequest request) {
		adminMemberUsecase.createMemberSemester(request);
	}
}
