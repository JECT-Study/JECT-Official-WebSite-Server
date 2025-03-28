package org.ject.support.domain.recruit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.dto.RecruitOpenedEvent;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class RecruitScheduleServiceTest {
    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private TaskScheduler recruitScheduler;

    @Test
    @DisplayName("recruit schedule end date")
    void test_schedule_end_date_of_recruit() {
        // given
        Recruit recruit = Recruit.builder()
                .semester("2025-1")
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        recruitRepository.save(recruit);

        // 스케줄 등록 직후
        Boolean beforeScheduleFinishedFlag = Boolean.valueOf(redisTemplate.opsForValue().get(Constants.PERIOD_FLAG));
        assertThat(beforeScheduleFinishedFlag).isTrue();

        // scheduling 매개변수 stubbing
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(recruitScheduler).schedule(runnableCaptor.capture(), instantCaptor.capture());

        runnableCaptor.getValue().run();

        // 스케줄링 동작 후
        Boolean afterScheduleFinishedFlag = Boolean.valueOf(redisTemplate.opsForValue().get(Constants.PERIOD_FLAG));
        assertThat(afterScheduleFinishedFlag).isFalse();

    }
}
