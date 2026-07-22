package org.ject.support.admin.member.service;

import java.util.List;

import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.MemberMakersListRequest;
import org.ject.support.admin.member.dto.response.MemberMakersListResponse;
import org.ject.support.admin.member.dto.result.MemberMakersListPageResult;
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

	@Transactional(readOnly = true)
	public CursorPageResponse<MemberMakersListResponse> getMemberMakersList(MemberMakersListRequest request) {
		// 목록 조회
		MemberMakersListPageResult pageResult = adminMemberActivityService.getMemberMakersList(request);

		// 페이징 값 처리
		boolean hasNext = pageResult.content().size() > request.getSizeOrDefault();
		long totalCount = pageResult.totalCount();
		List<MemberMakersListProjection> content = hasNext
			? pageResult.content().subList(0, request.getSizeOrDefault())
			: pageResult.content();

		List<MemberMakersListResponse> responses = content.stream()
			.map(MemberMakersListResponse::from)
			.toList();

		Long nextCursor = hasNext && !content.isEmpty()
			? content.get(content.size() - 1).memberActivityId()
			: null;

		// 응답
		return CursorPageResponse.of(
			responses,
			request.getSizeOrDefault(),
			hasNext,
			nextCursor,
			totalCount
		);
	}
}
