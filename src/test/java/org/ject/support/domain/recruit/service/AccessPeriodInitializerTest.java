package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessPeriodInitializerTest extends UnitTestSupport {

    @InjectMocks
    AccessPeriodInitializer accessPeriodInitializer;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    RecruitFlagService recruitFlagService;

    @Test
    void 애플리케이션_구동_시_활성_모집의_recruit_flag를_세팅한다() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        when(recruitRepository.findActiveRecruits(any(LocalDateTime.class))).thenReturn(List.of(recruit));

        // when
        accessPeriodInitializer.run(null);

        // then
        verify(recruitFlagService).setRecruitFlag(recruit);
    }
}
