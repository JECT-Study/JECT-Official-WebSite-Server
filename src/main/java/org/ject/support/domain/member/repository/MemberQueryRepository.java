package org.ject.support.domain.member.repository;

import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberResponse;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);

    List<String> findEmailsByIdsAndNotApply(List<Long> applicantIds);

    Page<MemberResponse> findMembers(final Role role, final JobFamily jobFamily, final Long semesterId, final Pageable pageable);
}
