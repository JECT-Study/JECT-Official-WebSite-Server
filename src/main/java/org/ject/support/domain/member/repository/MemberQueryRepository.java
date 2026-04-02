package org.ject.support.domain.member.repository;

import java.util.List;
import org.ject.support.domain.member.dto.TeamMemberNames;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);

    List<String> findEmailsByIdsAndNotSubmitted(List<Long> applicantIds);
}
