package org.ject.support.admin.member.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminMemberQueryRepository {

    Page<MemberProjection> findMembers(Role role, JobFamily jobFamily, Long semesterId, Pageable pageable);
}
