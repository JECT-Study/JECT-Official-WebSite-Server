package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.tempapply.service.RemindApplyService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RecruitScheduleService {

    private final TaskScheduler recruitScheduler;
    private final RecruitFlagService recruitFlagService;
    private final RemindApplyService remindApplyService;

    public void scheduleRecruitOpen(Recruit recruit) {
        Instant triggerTime = recruit.getStartDate()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        recruitScheduler.schedule(() -> {
            recruitFlagService.setRecruitFlag(recruit);
        }, triggerTime);
    }

    public void scheduleRemindApply(Recruit recruit) {
        Instant triggerTime = recruit.getEndDate()
                .minusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        recruitScheduler.schedule(() -> {
            remindApplyService.remindApply(recruit.getId());
        }, triggerTime);
    }
}
