package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberSupportersListRequest;
import org.ject.support.admin.member.dto.response.MemberSupportersListResponse;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberSupportersUseCase {

	private final AdminMemberService adminMemberService;
	private final AdminMemberActivityService adminMemberActivityService;

	// 운영 서포터즈 구성원 추가
	@Transactional
	public void createMemberSupporters(CreateMemberSupportersRequest request) {
		// TODO: ACTIVE 구성원 동시 등록 경쟁 조건 점검 및 해결 필요
		// email 기준 기존 Member 조회 또는 신규 생성
		Long memberId = adminMemberService.findOrCreateMember(request);

		// memberId 기준 MemberActivity와 MemberSupporters 생성 및 저장
		adminMemberActivityService.createMemberSupportersActivity(request, memberId);
	}

	@Transactional(readOnly = true)
	public CursorPageResponse<MemberSupportersListResponse> getMemberSupportersList(MemberSupportersListRequest request) {
		// 목록 조회
		MemberPageResult<MemberSupportersListProjection> pageResult =
			adminMemberActivityService.getMemberSupportersList(request);

		return pageResult.toCursorPageResponse(
			request.getSizeOrDefault(),
			MemberSupportersListResponse::from,
			MemberSupportersListProjection::memberActivityId
		);
	}
}
