package org.ject.support.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OngoingSemesterProvider {
    private final SemesterRepository semesterRepository;

    public Long getOngoingSemesterId() {
        return semesterRepository.findOngoingSemesterId()
                .orElseThrow(() -> new SemesterException(SemesterErrorCode.ONGOING_SEMESTER_NOT_FOUND));
    }
}
