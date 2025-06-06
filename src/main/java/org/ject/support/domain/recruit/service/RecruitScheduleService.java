package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.tempapply.service.RemindApplyService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class RecruitScheduleService {

    private static final String RECRUIT_OPEN_JOB_KEY_PREFIX = "recruit_open:";
    private static final String REMIND_APPLY_JOB_KEY_PREFIX = "remind_apply:";

    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private final TaskScheduler recruitScheduler;
    private final RecruitFlagService recruitFlagService;
    private final RemindApplyService remindApplyService;

    public void scheduleRecruitOpen(Recruit recruit) {
        Instant triggerTime = recruit.getStartDate()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        scheduledJobs.put(RECRUIT_OPEN_JOB_KEY_PREFIX + recruit.getId(), recruitScheduler.schedule(() -> {
            recruitFlagService.setRecruitFlag(recruit);
        }, triggerTime));
    }

    public void scheduleRemindApply(Recruit recruit) {
        Instant triggerTime = recruit.getEndDate()
                .minusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        scheduledJobs.put(REMIND_APPLY_JOB_KEY_PREFIX + recruit.getId(), recruitScheduler.schedule(() -> {
            remindApplyService.remindApply(recruit.getId());
        }, triggerTime));
    }

    public void cancelJobs(Long recruitId) {
        ScheduledFuture<?> removedRecruitOpenJob = scheduledJobs.remove(RECRUIT_OPEN_JOB_KEY_PREFIX + recruitId);
        if (removedRecruitOpenJob != null) {
            removedRecruitOpenJob.cancel(false);
        }

        ScheduledFuture<?> removedRemindApplyJob = scheduledJobs.remove(REMIND_APPLY_JOB_KEY_PREFIX + recruitId);
        if (removedRemindApplyJob != null) {
            removedRemindApplyJob.cancel(false);
        }
    }
}
