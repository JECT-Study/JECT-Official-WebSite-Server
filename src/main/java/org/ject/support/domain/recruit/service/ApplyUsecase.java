package org.ject.support.domain.recruit.service;

import java.util.Map;
import org.ject.support.domain.member.JobFamily;

public interface ApplyUsecase {
    Map<String, String> getTemporaryApplication(Long memberId);

    void applyTemporary(JobFamily jobFamily, Long memberId, Map<String, String> answers);


    void changeJobFamily(Long memberId, JobFamily newJobFamily);
}
