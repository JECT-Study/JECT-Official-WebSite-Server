package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.DeleteMembersRequest;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.response.MemberSupportersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberSupportersListResponse;
import org.ject.support.common.response.CursorPageResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Operation(
		summary = "운영 서포터즈 구성원 목록 조회",
		description = "운영 서포터즈 구성원 목록을 조회합니다."
	)
	CursorPageResponse<MemberSupportersListResponse> getAdminMemberSupportersList(@ModelAttribute MemberSupportersListRequest request);

	@Operation(
		summary = "운영 서포터즈 구성원 상세 조회",
		description = "운영 서포터즈 구성원을 상세 조회합니다."
	)
	MemberSupportersDetailResponse getAdminMemberSupportersDetail(@PathVariable Long memberActivityId);

	@Operation(
		summary = "운영 서포터즈 구성원 삭제",
		description = "전달한 활동 ID에 해당하는 운영 서포터즈 구성원을 삭제합니다."
	)
	void deleteAdminMemberSupporters(@PathVariable Long memberActivityId);

	@Operation(
		summary = "운영 서포터즈 구성원 일괄 삭제",
		description = "선택한 운영 서포터즈 구성원을 일괄 삭제합니다. 유효하지 않은 ID가 있으면 전체 요청이 실패합니다."
	)
	void deleteAdminMemberSupportersList(@RequestBody @Valid DeleteMembersRequest request);
}
