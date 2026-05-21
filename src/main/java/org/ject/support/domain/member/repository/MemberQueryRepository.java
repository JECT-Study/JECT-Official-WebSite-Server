package org.ject.support.domain.member.repository;

import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.domain.member.dto.MemberAccountProjection;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberQueryRepository {

    TeamMemberNames findMemberNamesByTeamId(Long teamId);

    List<String> findEmailsByIdsAndNotSubmitted(List<Long> applicantIds);

    Page<MemberAccountProjection> findAccounts(AdminAccountSearchCondition condition, Pageable pageable);
}
