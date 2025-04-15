package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.ject.support.domain.recruit.dto.Constants.RECRUIT_FLAG_PREFIX;

@Service
@RequiredArgsConstructor
public class RecruitFlagService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setRecruitFlag(Recruit recruit) {
        redisTemplate.opsForValue().set(
                String.format("%s%s", RECRUIT_FLAG_PREFIX, recruit.getJobFamily().name()),
                Boolean.TRUE.toString(),
                Duration.between(LocalDateTime.now(), recruit.getEndDate())
        );
    }

    public void deleteRecruitFlag(String recruitFlag) {
        redisTemplate.delete(recruitFlag);
    }
}
