package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.SemesterRegistered;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitRegisterService {
    private final RecruitRepository recruitRepository;
    private final SemesterRepository semesterRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRegisterRecruitEvent(SemesterRegistered event) throws DataIntegrityViolationException {
        Long semesterId = event.semesterId();
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new SemesterException(SemesterErrorCode.NOT_FOUND_SEMESTER));
        List<Recruit> recruits = event.recruitRegisterRequests().stream()
                .map(request -> request.toEntity(semester))
                .toList();
        recruitRepository.saveAll(recruits);
    }
}
