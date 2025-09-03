package org.ject.support.domain.recruit.service;

import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.RecruitSavedEvent;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class RecruitSavedEventHandlerTest extends UnitTestSupport {

    @InjectMocks
    RecruitSavedEventHandler recruitSavedEventHandler;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    RecruitFlagService recruitFlagService;

    @Mock
    RecruitScheduleService recruitScheduleService;

    Recruit recruit;

    @BeforeEach
    void setUp() {
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semesterId(1L)
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.of(recruit));
    }

    @Test
    @DisplayName("등록된 모집이 활성화되어 있다면 즉시 flag 캐싱 메서드 호출")
    void call_set_recruit_flag_method() {
        // when
        recruitSavedEventHandler.handleRecruitSaved(new RecruitSavedEvent(1L));

        // then
        verify(recruitFlagService, atMostOnce()).setRecruitFlag(recruit);
    }

    @Test
    @DisplayName("등록된 모집의 시작일이 미래 시점이라면 스케줄 등록 메서드 호출")
    void call_schedule_open() {
        // when
        recruitSavedEventHandler.handleRecruitSaved(new RecruitSavedEvent(1L));

        // then
        verify(recruitScheduleService, atMostOnce()).scheduleRecruitOpen(recruit);
    }
}