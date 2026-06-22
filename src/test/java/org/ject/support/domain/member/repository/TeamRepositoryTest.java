package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.ject.support.domain.member.entity.Team;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class TeamRepositoryTest {

	@Autowired
	private TeamRepository teamRepository;

	@Test
	void 기수_ID로_팀_ID_목록을_조회한다() {
		// given
		Team firstTeam = teamRepository.save(Team.builder().name("1팀").semesterId(1L).build());
		Team secondTeam = teamRepository.save(Team.builder().name("2팀").semesterId(1L).build());
		teamRepository.save(Team.builder().name("다른 기수 팀").semesterId(2L).build());

		// when
		List<Long> result = teamRepository.findIdsBySemesterId(1L);

		// then
		assertThat(result).containsExactlyInAnyOrder(firstTeam.getId(), secondTeam.getId());
	}
}
