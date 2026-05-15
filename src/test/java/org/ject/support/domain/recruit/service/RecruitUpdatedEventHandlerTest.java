package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.RecruitUpdatedEvent;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruitUpdatedEventHandlerTest extends UnitTestSupport {

    @InjectMocks
    RecruitUpdatedEventHandler recruitUpdatedEventHandler;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    RecruitFlagService recruitFlagService;

    @Mock
    RecruitScheduleService recruitScheduleService;

    Recruit futureRecruit;

    @BeforeEach
    void setUp() {
        futureRecruit = Recruit.builder()
                .id(1L)
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();
        when(recruitRepository.findById(1L)).thenReturn(Optional.of(futureRecruit));
    }

    @Test
    void 시작일이_미래로_변경되면_기존_recruit_flag를_제거하고_오픈_스케줄을_등록한다() {
        // when
        recruitUpdatedEventHandler.handleRecruitUpdated(new RecruitUpdatedEvent(
                1L,
                JobFamily.BE,
                futureRecruit.getStartDate(),
                futureRecruit.getEndDate()));

        // then
        verify(recruitFlagService).deleteRecruitFlag(1L, JobFamily.BE);
        verify(recruitScheduleService).scheduleRecruitOpen(futureRecruit);
        verify(recruitFlagService, never()).setRecruitFlag(futureRecruit);
    }
}
