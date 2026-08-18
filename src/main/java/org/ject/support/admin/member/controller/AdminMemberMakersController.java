package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.DeleteMembersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.response.MemberMakersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberMakersListResponse;
import org.ject.support.admin.member.service.AdminMemberMakersUseCase;
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
@RequestMapping("/admin/members/makers")
@RequiredArgsConstructor
public class AdminMemberMakersController implements AdminMemberMakersApiSpec {

	private final AdminMemberMakersUseCase adminMemberMakersUseCase;

	// 메이커스팀 구성원 추가
	@Override
	@PostMapping
	public void createAdminMemberMakers(
		@RequestBody @Valid CreateMemberMakersRequest request) {
		adminMemberMakersUseCase.createMemberMakers(request);
	}

	@Override
	@GetMapping
	public CursorPageResponse<MemberMakersListResponse> getAdminMemberMakersList(
		@ParameterObject @ModelAttribute @Valid MemberMakersListRequest request) {
		return adminMemberMakersUseCase.getMemberMakersList(request);
	}

	@Override
	@GetMapping("/{memberActivityId}")
	public MemberMakersDetailResponse getAdminMemberMakersDetail(@PathVariable Long memberActivityId) {
		return adminMemberMakersUseCase.getMemberMakersDetail(memberActivityId);
	}

	@Override
	@DeleteMapping("/{memberActivityId}")
	public void deleteAdminMemberMakers(@PathVariable Long memberActivityId) {
		adminMemberMakersUseCase.deleteMemberMakers(memberActivityId);
	}

	@Override
	@DeleteMapping
	public void deleteAdminMemberMakersList(@RequestBody @Valid DeleteMembersRequest request) {
		adminMemberMakersUseCase.deleteMemberMakersList(request);
	}

}
