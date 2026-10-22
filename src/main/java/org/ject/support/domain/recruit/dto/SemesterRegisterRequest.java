package org.ject.support.domain.recruit.dto;

import jakarta.validation.Valid;
import java.util.List;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;

public record SemesterRegisterRequest(String name,
                                      List<@Valid RecruitRegisterRequest> recruitRegisterRequests) {
    public void validate() {
        long distinctCount = this.recruitRegisterRequests.stream()
                .map(RecruitRegisterRequest::jobFamily)
                .distinct()
                .count();

        if (distinctCount < this.recruitRegisterRequests.size()) {
            throw new RecruitException(RecruitErrorCode.DUPLICATED_JOB_FAMILY);
        }
    }
}
