package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.RecruitUpdatedEvent;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class RecruitUpdatedEventHandler {

    private final RecruitRepository recruitRepository;
    private final RecruitFlagService recruitFlagService;
    private final RecruitScheduleService recruitScheduleService;

    /**
     * 모집 수정 시 호출됨
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecruitUpdated(RecruitUpdatedEvent event) {
        // 기존 스케줄 작업 제거
        recruitScheduleService.cancelJobs(event.recruitId());

        // 수정된 모집 정보 조회
        Recruit recruit = recruitRepository.findById(event.recruitId())
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));

        if (recruit.isRecruitingPeriod()) {
            // 등록된 모집이 활성화되어 있다면 즉시 flag 캐싱
            recruitFlagService.setRecruitFlag(recruit);
        } else {
            // 등록된 모집의 시작일이 미래 시점이라면 스케줄 등록
            recruitScheduleService.scheduleRecruitOpen(recruit);
        }

        // 모집 마감 하루 전 리마인드 스케줄 등록
        recruitScheduleService.scheduleRemindApply(recruit);
    }
}
