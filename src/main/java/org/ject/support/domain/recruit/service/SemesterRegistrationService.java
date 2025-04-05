package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.SemesterRegisterRequest;
import org.ject.support.domain.recruit.dto.SemesterRegistered;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SemesterRegistrationService implements SemesterRegisterUsecase {
    private final SemesterRepository semesterRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public void registerSemester(final SemesterRegisterRequest request) {
        request.validate();

        // semester 등록
        Semester semester = Semester.builder()
                .name(request.name())
                .build();

        semesterRepository.save(semester);
        // semester 기준으로 recruit 등록
        eventPublisher.publishEvent(new SemesterRegistered(semester.getId(), request.recruitRegisterRequests()));
    }
}
