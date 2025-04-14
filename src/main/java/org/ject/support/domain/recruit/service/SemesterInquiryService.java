package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.SemesterResponse;
import org.ject.support.domain.recruit.dto.SemesterResponses;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SemesterInquiryService implements SemesterInquiryUsecase {

    private final SemesterRepository semesterRepository;

    @Override
    @Cacheable(value = "semester", key = "'all'")
    @Transactional(readOnly = true)
    public SemesterResponses getAllSemesters() {
        return new SemesterResponses(semesterRepository.findAll()
                .stream()
                .map(SemesterResponse::from)
                .toList());
    }
}
