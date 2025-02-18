package org.ject.support.domain.recruit.service;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 기한 외 접근 제한을 위한 flag caching 초기화
 */
@Component
@RequiredArgsConstructor
public class AccessPeriodInitializer implements ApplicationRunner {
    private final RedisTemplate<String, String> redisTemplate;
    private final RecruitRepository recruitRepository;

    @Override
    public void run(final ApplicationArguments args) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(Constants.PERIOD_FLAG))) {// flag가 저장되어있지 않으면
            recruitRepository.findByStartDateAfterAndEndDateBefore(LocalDateTime.now())
                            .ifPresent(this::setRecruitFlag);

        }
    }

    private void setRecruitFlag(final Recruit recruit) {
        redisTemplate.opsForValue().set(Constants.PERIOD_FLAG, Boolean.toString(true),// 존재여부를 redis에 저장
                Duration.between(recruit.getStartDate(), recruit.getEndDate()));// ttl은 지원 기간동안 살아있도록
    }
}
