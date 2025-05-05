package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.dto.TeamMemberNames;

import java.util.List;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);

    List<String> findEmailsByIdsAndNotApply(List<Long> applicantIds);
}
