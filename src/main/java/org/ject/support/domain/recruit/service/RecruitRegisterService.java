package org.ject.support.domain.recruit.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.SemesterRegistered;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class RecruitRegisterService {
    private final RecruitRepository recruitRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRegisterRecruitEvent(SemesterRegistered event) throws DataIntegrityViolationException {
        Long semesterId = event.semesterId();
        List<Recruit> recruits = event.recruitRegisterRequests().stream()
                .map(request -> request.toEntity(semesterId))
                .toList();
        recruitRepository.saveAll(recruits);
    }
}
