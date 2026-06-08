package org.ject.support.domain.member.repository;

import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.domain.member.dto.MemberAccountProjection;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);

    Page<MemberAccountProjection> findAccounts(AdminAccountSearchCondition condition, Pageable pageable);
}
