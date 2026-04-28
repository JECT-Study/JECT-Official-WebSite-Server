package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

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
        for (JobFamily jobFamily : JobFamily.values()) {
            setRecruitFlag(jobFamily);
        }
    }

    private void setRecruitFlag(JobFamily jobFamily) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(getKey(jobFamily)))) {
            recruitRepository.findActiveRecruitByJobFamily(jobFamily, LocalDateTime.now())
                    .ifPresent(recruit -> redisTemplate.opsForValue().set(
                            getKey(jobFamily), Boolean.toString(true),
                            Duration.between(recruit.getStartDate(), recruit.getEndDate())));
        }
    }

    private String getKey(JobFamily jobFamily) {
        return String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, jobFamily);
    }
}
