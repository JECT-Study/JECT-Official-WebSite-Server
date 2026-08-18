package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.DeleteMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.response.MemberSupportersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberSupportersListResponse;
import org.ject.support.admin.member.service.AdminMemberSupportersUseCase;
import org.ject.support.common.response.CursorPageResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Override
	@GetMapping
	public CursorPageResponse<MemberSupportersListResponse> getAdminMemberSupportersList(
		@ParameterObject @ModelAttribute @Valid MemberSupportersListRequest request) {
		return adminMemberSupportersUseCase.getMemberSupportersList(request);
	}

	@Override
	@GetMapping("/{memberActivityId}")
	public MemberSupportersDetailResponse getAdminMemberSupportersDetail(@PathVariable Long memberActivityId) {
		return adminMemberSupportersUseCase.getMemberSupportersDetail(memberActivityId);
	}

	@Override
	@DeleteMapping("/{memberActivityId}")
	public void deleteAdminMemberSupporters(@PathVariable Long memberActivityId) {
		adminMemberSupportersUseCase.deleteMemberSupporters(memberActivityId);
	}

	@Override
	@DeleteMapping
	public void deleteAdminMemberSupportersList(@RequestBody @Valid DeleteMemberSupportersRequest request) {
		adminMemberSupportersUseCase.deleteMemberSupportersList(request);
	}
}
