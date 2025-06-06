package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.RecruitCanceledEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.ject.support.domain.recruit.dto.Constants.RECRUIT_FLAG_PREFIX;

@Service
@RequiredArgsConstructor
public class RecruitCanceledEventHandler {

    private final RecruitFlagService recruitFlagService;
    private final RecruitScheduleService recruitScheduleService;

    /**
     * 모집 취소 시 호출됨
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecruitCanceled(RecruitCanceledEvent event) {
        recruitFlagService.deleteRecruitFlag(String.format("%s%s", RECRUIT_FLAG_PREFIX, event.jobFamily()));
        recruitScheduleService.cancelJobs(event.recruitId());
    }
}