package org.ject.support.domain.recruit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.SemesterResponse;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SemesterInquiryServiceTest {

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private SemesterInquiryService semesterInquiryService;

    @Test
    @DisplayName("모집 공고에 연결된 기수 ID를 조회한다")
    void 모집_공고에_연결된_기수_ID를_조회한다() {
        // given
        Long recruitId = 22L;
        Long semesterId = 5L;
        given(semesterRepository.findSemesterIdByRecruitId(recruitId))
                .willReturn(Optional.of(semesterId));

        // when
        Long result = semesterInquiryService.getSemesterIdByRecruitId(recruitId);

        // then
        assertThat(result).isEqualTo(semesterId);
    }

    @Test
    @DisplayName("존재하지 않는 모집 공고로 기수를 조회하면 예외가 발생한다")
    void 존재하지_않는_모집_공고로_기수를_조회하면_예외가_발생한다() {
        // given
        Long recruitId = 22L;
        given(semesterRepository.findSemesterIdByRecruitId(recruitId))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(
                () -> semesterInquiryService.getSemesterIdByRecruitId(recruitId));

        // then
        assertThat(throwable)
                .isInstanceOf(RecruitException.class)
                .extracting("errorCode")
                .isEqualTo(RecruitErrorCode.NOT_FOUND_RECRUIT);
    }

    @Test
    @DisplayName("기수 ID로 기수를 조회한다")
    void 기수_ID로_기수를_조회한다() {
        // given
        Long semesterId = 1L;
        Semester semester = Semester.builder()
            .id(semesterId)
            .name("5기")
            .build();
        given(semesterRepository.findById(semesterId)).willReturn(Optional.of(semester));

        // when
        SemesterResponse response = semesterInquiryService.getSemester(semesterId);

        // then
        assertThat(response.id()).isEqualTo(semesterId);
        assertThat(response.name()).isEqualTo(semester.getName());
    }

    @Test
    @DisplayName("존재하지 않는 기수 ID로 조회하면 예외가 발생한다")
    void 존재하지_않는_기수_ID로_조회하면_예외가_발생한다() {
        // given
        Long semesterId = 1L;
        given(semesterRepository.findById(semesterId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> semesterInquiryService.getSemester(semesterId));

        // then
        assertThat(throwable)
            .isInstanceOf(SemesterException.class)
            .extracting("errorCode")
            .isEqualTo(SemesterErrorCode.NOT_FOUND_SEMESTER);
    }
}
