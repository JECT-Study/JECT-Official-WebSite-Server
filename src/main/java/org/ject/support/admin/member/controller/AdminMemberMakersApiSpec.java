package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.DeleteMembersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.request.UpdateMemberMakersRequest;
import org.ject.support.admin.member.dto.response.MemberMakersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberMakersListResponse;
import org.ject.support.common.response.CursorPageResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Operation(
		summary = "메이커스팀 구성원 상세 조회",
		description = "메이커스팀 구성원을 상세 조회합니다."
	)
	MemberMakersDetailResponse getAdminMemberMakersDetail(@PathVariable Long memberActivityId);

	@Operation(
		summary = "메이커스팀 구성원 목록 조회",
		description = "메이커스팀 구성원 목록을 조회합니다."
	)
	CursorPageResponse<MemberMakersListResponse> getAdminMemberMakersList(@ModelAttribute MemberMakersListRequest request);

	@Operation(
		summary = "메이커스팀 구성원 수정",
		description = "전달한 활동 ID에 해당하는 메이커스팀 구성원의 입력된 정보만 수정합니다."
	)
	void editAdminMemberMakers(@PathVariable Long memberActivityId, @RequestBody @Valid UpdateMemberMakersRequest request);

	@Operation(
		summary = "메이커스팀 구성원 삭제",
		description = "전달한 활동 ID에 해당하는 메이커스팀 구성원을 삭제합니다."
	)
	void deleteAdminMemberMakers(@PathVariable Long memberActivityId);

	@Operation(
		summary = "메이커스팀 구성원 일괄 삭제",
		description = "선택한 메이커스팀 구성원을 일괄 삭제합니다. 유효하지 않은 ID가 있으면 전체 요청이 실패합니다."
	)
	void deleteAdminMemberMakersList(@RequestBody @Valid DeleteMembersRequest request);
}
