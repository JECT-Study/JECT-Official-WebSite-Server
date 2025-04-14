package org.ject.support.domain.recruit.service;

import org.assertj.core.api.Assertions;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;

@ExtendWith(MockitoExtension.class)
class RecruitServiceTest {

    @InjectMocks
    RecruitService recruitService;

    @Mock
    RecruitRepository recruitRepository;

    @Test
    @DisplayName("마감된 모집 정보는 수정 불가")
    void update_not_allow_for_closed_recruit() {
        // given
        Recruit recruit = Recruit.builder()
                .semesterId(1L)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .isClosed(true)
                .build();

        Mockito.when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        RecruitUpdateRequest request
                = new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        Assertions.assertThatThrownBy(() -> recruitService.updateRecruit(1L, request))
                .isInstanceOf(RecruitException.class);
    }
}