package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.ject.support.domain.recruit.dto.Constants.RECRUIT_FLAG_PREFIX;

@Service
@RequiredArgsConstructor
public class RecruitFlagService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setRecruitFlag(Recruit recruit) {
        Duration timeToLive = Duration.between(LocalDateTime.now(), recruit.getEndDate());
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            return;
        }

        if (recruit.getId() != null) {
            redisTemplate.opsForValue().set(getRecruitFlagKey(recruit.getId()), Boolean.TRUE.toString(), timeToLive);
        }
        redisTemplate.opsForValue().set(getRecruitFlagKey(recruit.getJobFamily()), Boolean.TRUE.toString(), timeToLive);
    }

    public void deleteRecruitFlag(Long recruitId, JobFamily jobFamily) {
        redisTemplate.delete(List.of(getRecruitFlagKey(recruitId), getRecruitFlagKey(jobFamily)));
    }

    private String getRecruitFlagKey(Long recruitId) {
        return String.format("%s%s", RECRUIT_FLAG_PREFIX, recruitId);
    }

    private String getRecruitFlagKey(JobFamily jobFamily) {
        return String.format("%s%s", RECRUIT_FLAG_PREFIX, jobFamily.name());
    }
}
