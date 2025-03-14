package org.ject.support.domain.recruit.service;

import java.util.List;
import java.util.Map;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.ApplyTemporaryResponse;

public interface ApplyUsecase {
    ApplyTemporaryResponse getTemporaryApplication(Long memberId);

    void applyTemporary(JobFamily jobFamily,
                        Long memberId,
                        Map<String, String> answers,
                        List<Map<String, String>> portfolios);


    void changeJobFamily(Long memberId, JobFamily newJobFamily);
}
