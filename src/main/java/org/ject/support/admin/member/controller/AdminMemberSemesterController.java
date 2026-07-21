package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.response.SearchMemberSemesterResponse;
import org.ject.support.admin.member.service.AdminMemberSemesterUseCase;
import org.ject.support.common.response.CursorPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/members/semester")
@RequiredArgsConstructor
public class AdminMemberSemesterController implements AdminMemberSemesterApiSpec {

	private final AdminMemberSemesterUseCase adminMemberUsecase;

	// 일반 구성원 추가
	@Override
	@PostMapping
	public void createAdminMemberSemester(
		@RequestBody @Valid CreateMemberSemesterRequest request) {
		adminMemberUsecase.createMemberSemester(request);
	}

	// 일반 구성원 목록 조회
	@Override
	@GetMapping
	public CursorPageResponse<SearchMemberSemesterResponse> searchAdminMemberSemesterList(
		@ModelAttribute @Valid MemberSemesterSearchCondition request) {
		return adminMemberUsecase.searchMemberSemester(request);
	}
}
