package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.service.OngoingSemesterProvider;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruitServiceTest extends UnitTestSupport {

    @InjectMocks
    RecruitService recruitService;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    SemesterRepository semesterRepository;

    @Mock
    OngoingSemesterProvider ongoingSemesterProvider;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void 모집_등록_성공() {
        // given
        Semester semester = getSemester(false);
        when(semesterRepository.findRecruitingSemester()).thenReturn(Optional.ofNullable(semester));
        when(recruitRepository.existsByJobFamilyAndIsNotClosed(eq(semester.getId()), any())).thenReturn(false);

        List<RecruitRegisterRequest> requests = List.of(
                new RecruitRegisterRequest(BE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)),
                new RecruitRegisterRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))
        );

        // when
        recruitService.registerRecruits(requests);

        // then
        ArgumentCaptor<List<JobFamily>> jobFamiliesCaptor = ArgumentCaptor.forClass(List.class);
        verify(recruitRepository).existsByJobFamilyAndIsNotClosed(eq(semester.getId()), jobFamiliesCaptor.capture());
        assertThat(jobFamiliesCaptor.getValue()).containsExactly(BE, FE);

        ArgumentCaptor<List<Recruit>> recruitsCaptor = ArgumentCaptor.forClass(List.class);
        verify(recruitRepository).saveAll(recruitsCaptor.capture());
        assertThat(recruitsCaptor.getValue()).hasSize(2);
    }

    private Semester getSemester() {
        Semester semester = Semester.builder()
                .id(1L)
                .name("1기")
                .isRecruiting(false)
                .build();
        return semester;
    }

    @Test
    void 모집_등록_시_이미_모집중인_직군이면_실패() {
        // given
        Semester semester = getSemester();
        RecruitRegisterRequest request =
                new RecruitRegisterRequest(BE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(semesterRepository.findRecruitingSemester()).thenReturn(Optional.ofNullable(semester));
        when(recruitRepository.existsByJobFamilyAndIsNotClosed(any(), any())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> recruitService.registerRecruits(List.of(request)))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 모집_정보_수정_성공() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build();
        when(recruitRepository.findById(1L)).thenReturn(Optional.of(recruit));

        // 직군은 FE로, 마감일은 3일 후로 수정
        LocalDateTime newEndDate = LocalDateTime.now().plusDays(3);
        RecruitUpdateRequest request = new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), newEndDate);

        // when
        recruitService.updateRecruit(recruit.getId(), request);

        // then
        assertThat(recruit.getJobFamily()).isEqualTo(FE);
        assertThat(recruit.getEndDate()).isEqualTo(newEndDate);
    }

    @Test
    void 마감된_모집_정보는_수정_불가() {
        // given
        Recruit recruit = Recruit.builder()
                .semester(getSemester(false))
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().minusDays(1))
                .jobFamily(BE)
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        RecruitUpdateRequest request
                = new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        assertThatThrownBy(() -> recruitService.updateRecruit(1L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 수정하려는_recruit이_존재하지_않으면_실패() {
        // given
        RecruitUpdateRequest request =
                new RecruitUpdateRequest(BE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        assertThatThrownBy(() -> recruitService.updateRecruit(100L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 수정_시점이_모집기간이_아닌_경우_실패() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(false))
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().minusDays(1))
                .jobFamily(BE)
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        RecruitUpdateRequest request
                = new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        assertThatThrownBy(() -> recruitService.updateRecruit(1L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 모집_취소_성공() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        // when
        recruitService.cancelRecruit(1L);

        // then
        ArgumentCaptor<Recruit> recruitCaptor = ArgumentCaptor.forClass(Recruit.class);
        verify(recruitRepository).delete(recruitCaptor.capture());
        assertThat(recruitCaptor.getValue()).isSameAs(recruit);
    }

    private Semester getSemester(boolean isRecruiting) {
        return Semester.builder().id(1L).name("1기").isRecruiting(isRecruiting).build();
    }

    @Test
    void 취소하려는_recruit이_존재하지_않으면_실패() {
        // when, then
        assertThatThrownBy(() -> recruitService.cancelRecruit(100L))
                .isInstanceOf(RecruitException.class);
    }
}