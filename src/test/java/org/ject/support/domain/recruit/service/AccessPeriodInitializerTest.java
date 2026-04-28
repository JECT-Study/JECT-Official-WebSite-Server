package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AccessPeriodInitializerTest extends UnitTestSupport {

    @InjectMocks
    private AccessPeriodInitializer accessPeriodInitializer;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RecruitRepository recruitRepository;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("애플리케이션 구동 시 모든 직군의 RECRUIT_FLAG를 세팅한다")
    void set_recruit_flag_by_run_application() {
        // given
        given(redisTemplate.hasKey(anyString())).willReturn(false);
        given(recruitRepository.findActiveRecruitByJobFamily(any(JobFamily.class), any(LocalDateTime.class)))
                .willAnswer(invocation -> {
                    JobFamily jobFamily = invocation.getArgument(0);
                    if (jobFamily == JobFamily.PM || jobFamily == JobFamily.PD || jobFamily == JobFamily.SUPPORTER) {
                        return Optional.of(recruit(jobFamily));
                    }
                    return Optional.empty();
                });

        // when
        accessPeriodInitializer.run(null);

        // then
        verify(recruitRepository).findActiveRecruitByJobFamily(eq(JobFamily.FE), any(LocalDateTime.class));
        verify(recruitRepository).findActiveRecruitByJobFamily(eq(JobFamily.BE), any(LocalDateTime.class));
        verify(valueOperations).set(eq(getRecruitFlagKey(JobFamily.PM)), eq(Boolean.toString(true)), any(Duration.class));
        verify(valueOperations).set(eq(getRecruitFlagKey(JobFamily.PD)), eq(Boolean.toString(true)), any(Duration.class));
        verify(valueOperations).set(eq(getRecruitFlagKey(JobFamily.SUPPORTER)), eq(Boolean.toString(true)), any(Duration.class));
        verify(valueOperations, never()).set(eq(getRecruitFlagKey(JobFamily.FE)), anyString(), any(Duration.class));
        verify(valueOperations, never()).set(eq(getRecruitFlagKey(JobFamily.BE)), anyString(), any(Duration.class));
    }

    private Recruit recruit(final JobFamily jobFamily) {
        return Recruit.builder()
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .build();
    }

    private String getRecruitFlagKey(final JobFamily jobFamily) {
        return String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, jobFamily);
    }
}
