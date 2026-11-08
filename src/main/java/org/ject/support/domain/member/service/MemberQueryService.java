package org.ject.support.domain.member.service;

import java.util.List;
import org.ject.support.domain.member.entity.MemberActivity;

public interface MemberQueryService {

    List<MemberActivity> searchActiveActivities(String email);
}
