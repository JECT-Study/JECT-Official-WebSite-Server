package org.ject.support.domain.recruit.service;

import java.util.Map;
import org.ject.support.domain.member.JobFamily;

public interface ApplyUsecase {
    void applyTemporary(JobFamily jobFamily, Long memberId, Map<String, String> answers);
}
