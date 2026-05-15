package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.ject.support.domain.recruit.dto.Constants.RECRUIT_FLAG_PREFIX;

@Service
@RequiredArgsConstructor
public class RecruitFlagService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RecruitRepository recruitRepository;

    public void setRecruitFlag(Recruit recruit) {
        Duration timeToLive = Duration.between(LocalDateTime.now(), recruit.getEndDate());
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            return;
        }

        if (recruit.getId() != null) {
            redisTemplate.opsForValue().set(getRecruitFlagKey(recruit.getId()), Boolean.TRUE.toString(), timeToLive);
        }
        refreshJobFamilyFlag(recruit.getJobFamily());
    }

    public void deleteRecruitFlag(Long recruitId, JobFamily jobFamily) {
        if (recruitId != null) {
            redisTemplate.delete(getRecruitFlagKey(recruitId));
        }
        refreshJobFamilyFlag(jobFamily);
    }

    private void refreshJobFamilyFlag(JobFamily jobFamily) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latestEndDate = recruitRepository.findLatestActiveRecruitEndDateByJobFamily(jobFamily, now);
        if (latestEndDate == null) {
            redisTemplate.delete(getRecruitFlagKey(jobFamily));
            return;
        }

        Duration timeToLive = Duration.between(now, latestEndDate);
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            redisTemplate.delete(getRecruitFlagKey(jobFamily));
            return;
        }

        redisTemplate.opsForValue().set(getRecruitFlagKey(jobFamily), Boolean.TRUE.toString(), timeToLive);
    }

    private String getRecruitFlagKey(Long recruitId) {
        return String.format("%s%s", RECRUIT_FLAG_PREFIX, recruitId);
    }

    private String getRecruitFlagKey(JobFamily jobFamily) {
        return String.format("%s%s", RECRUIT_FLAG_PREFIX, jobFamily.name());
    }
}
