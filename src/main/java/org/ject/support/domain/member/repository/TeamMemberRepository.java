package org.ject.support.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.domain.member.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByMemberId(Long memberId);

    Optional<TeamMember> findByMemberIdAndTeamSemesterId(Long memberId, Long semesterId);
}
