package org.ject.support.admin.member.service;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.response.MemberMakersDetailResponse;
import org.ject.support.admin.member.dto.response.MemberMakersListResponse;
import org.ject.support.admin.member.dto.result.MemberPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberMakersUseCase {

	private final AdminMemberService adminMemberService;
	private final AdminMemberActivityService adminMemberActivityService;

	// 메이커스팀 구성원 추가
	@Transactional
	public void createMemberMakers(CreateMemberMakersRequest request) {
		// email 기준 기존 Member 조회 또는 신규 생성
		Long memberId = adminMemberService.findOrCreateMember(request);

		// memberId 기준 MemberActivity와 MemberMakers 생성 및 저장
		adminMemberActivityService.createMemberMakersActivity(request, memberId);
	}

	// 메이커스팀 구성원 목록 조회(커서 기반 페이징)
	@Transactional(readOnly = true)
	public CursorPageResponse<MemberMakersListResponse> getMemberMakersList(MemberMakersListRequest request) {
		// 목록 조회
		MemberPageResult<MemberMakersListProjection> pageResult =
			adminMemberActivityService.getMemberMakersList(request);

		return pageResult.toCursorPageResponse(
			request.getSizeOrDefault(),
			MemberMakersListResponse::from,
			MemberMakersListProjection::memberActivityId
		);
	}

	// 메이커스팀 구성원 상세조회
	@Transactional(readOnly = true)
	public MemberMakersDetailResponse getMemberMakersDetail(Long memberActivityId) {
		MemberMakersDetailProjection projection = adminMemberActivityService.getMemberMakersDetail(memberActivityId);
		return MemberMakersDetailResponse.from(projection);
	}
}
