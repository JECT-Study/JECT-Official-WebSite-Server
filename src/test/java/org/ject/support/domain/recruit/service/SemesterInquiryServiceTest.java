package org.ject.support.domain.recruit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.SemesterResponse;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
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
