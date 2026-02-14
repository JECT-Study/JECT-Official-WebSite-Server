package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByMemberId(Long memberId);
}
