package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;

import org.ject.support.domain.member.repository.TeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberTeamServiceTest {

	@Mock
	private TeamRepository teamRepository;

	@InjectMocks
	private AdminMemberTeamService adminMemberTeamService;

	@Test
	@DisplayName("기수에 속한 팀 ID 목록을 조회한다")
	void 기수에_속한_팀_ID_목록을_조회한다() {
		// given
		Long semesterId = 1L;
		List<Long> teamIds = List.of(1L, 2L);
		given(teamRepository.findIdsBySemesterId(semesterId)).willReturn(teamIds);

		// when
		List<Long> result = adminMemberTeamService.getTeamIdsBySemesterId(semesterId);

		// then
		assertThat(result).containsExactlyElementsOf(teamIds);
		verify(teamRepository).findIdsBySemesterId(semesterId);
	}
}
