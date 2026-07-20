package org.ject.support.admin.member.service;

import java.util.List;

import org.ject.support.domain.member.repository.TeamRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberTeamService {

	private final TeamRepository teamRepository;

	// 기수에 속한 팀 ID 목록 조회
	public List<Long> getTeamIdsBySemesterId(Long semesterId) {
		return teamRepository.findIdsBySemesterId(semesterId);
	}
}
