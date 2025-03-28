package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.dto.RecruitOpenedEvent;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RecruitScheduleService {

    private final TaskScheduler recruitScheduler;
    private final RecruitRepository recruitRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 지원기간이면 호출됨
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecruitOpened(RecruitOpenedEvent event) {
        Recruit recruit = getRecruit(event);
        System.out.println("recruit.getId() = " + recruit.getId());

        //end date 전에는 flag를 true로
        redisTemplate.opsForValue().set(Constants.PERIOD_FLAG, Boolean.TRUE.toString());

        Instant triggerTime = recruit.getEndDate()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        recruitScheduler.schedule(() -> {
            //end date에 도달하면 redis의 flag를 false로 변경
            redisTemplate.opsForValue().set(Constants.PERIOD_FLAG, Boolean.FALSE.toString());
        }, triggerTime);
    }

    private Recruit getRecruit(final RecruitOpenedEvent event) {
        Long recruitId = event.recruitId();
        return recruitRepository.findById(recruitId)
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND));
    }
}
