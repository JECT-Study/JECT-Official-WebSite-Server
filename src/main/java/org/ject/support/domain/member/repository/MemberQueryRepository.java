package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.dto.TeamMemberNames;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);
}
