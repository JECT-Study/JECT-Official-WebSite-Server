package org.ject.support.admin.member.controller;

import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.DeleteMembersRequest;
import org.ject.support.admin.member.dto.request.EditEventParticipationRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.response.SearchMemberSemesterResponse;
import org.ject.support.admin.member.dto.response.MemberSemesterResponse;
import org.ject.support.common.response.CursorPageResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Operation(
		summary = "일반 구성원 단건 조회",
		description = "일반 구성원 정보와 기수별 행사 참여 상태를 조회합니다."
	)
	MemberSemesterResponse getMemberSemester(@PathVariable Long memberActivityId);

	@Operation(
		summary = "일반 구성원 리스트 조회",
		description = "일반 구성원 목록을 커서기반 페이징과 필터를 적용해 조회합니다."
	)
	CursorPageResponse<SearchMemberSemesterResponse> searchAdminMemberSemesterList(
		@ModelAttribute @Valid MemberSemesterSearchCondition request
	);

	@Operation(
		summary = "일반 구성원 행사 참여 상태 수정",
		description = "일반 구성원의 기수별 행사 참여 상태를 수정합니다. 참여 상태가 null이면 미지정 처리합니다."
	)
	void editEventParticipation(@PathVariable Long memberActivityId, @RequestBody @Valid EditEventParticipationRequest request);

	@Operation(
		summary = "일반 구성원 삭제",
		description = "일반 구성원을 삭제합니다."
	)
	void deleteAdminMemberSemester(@PathVariable Long memberActivityId);

	@Operation(
		summary = "일반 구성원 일괄 삭제",
		description = "선택한 일반 구성원을 일괄 삭제합니다."
	)
	void deleteAdminMemberSemesterList(@RequestBody @Valid DeleteMembersRequest request);
}
