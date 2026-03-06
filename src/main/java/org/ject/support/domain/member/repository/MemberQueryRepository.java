package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberProjection;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);

    List<String> findEmailsByIdsAndNotSubmitted(List<Long> applicantIds);

    Page<MemberProjection> findMembers(final Role role, final JobFamily jobFamily, final Long semesterId, final Pageable pageable);
}
