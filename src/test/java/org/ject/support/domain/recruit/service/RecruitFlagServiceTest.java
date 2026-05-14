package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RecruitFlagServiceTest extends UnitTestSupport {

    @InjectMocks
    RecruitFlagService recruitFlagService;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 모집_공고와_직군_기준_recruit_flag를_함께_세팅한다() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        // when
        recruitFlagService.setRecruitFlag(recruit);

        // then
        verify(valueOperations).set(eq("RECRUIT_FLAG:1"), eq("true"), any(Duration.class));
        verify(valueOperations).set(eq("RECRUIT_FLAG:BE"), eq("true"), any(Duration.class));
    }

    @Test
    void 종료된_모집은_recruit_flag를_세팅하지_않는다() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();

        // when
        recruitFlagService.setRecruitFlag(recruit);

        // then
        verify(valueOperations, never()).set(any(), any(), any(Duration.class));
    }

    @Test
    void 모집_공고와_직군_기준_recruit_flag를_함께_제거한다() {
        // when
        recruitFlagService.deleteRecruitFlag(1L, JobFamily.BE);

        // then
        verify(redisTemplate).delete(List.of("RECRUIT_FLAG:1", "RECRUIT_FLAG:BE"));
    }
}
