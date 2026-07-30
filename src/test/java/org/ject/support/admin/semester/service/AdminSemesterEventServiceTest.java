package org.ject.support.admin.semester.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.ject.support.domain.recruit.domain.SemesterEventType.EVENT;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.SemesterEventRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class AdminSemesterEventServiceTest extends UnitTestSupport {

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private SemesterEventRepository semesterEventRepository;

    @InjectMocks
    private AdminSemesterEventService adminSemesterEventService;

    @BeforeEach
    void setUp() {
        given(semesterRepository.existsById(anyLong())).willReturn(true);
    }

    @Test
    @DisplayName("선택한 기수와 행사 유형의 행사 목록을 조회한다")
    void 선택한_기수와_행사_유형의_행사_목록을_조회한다() {
        // given
        SemesterEvent semesterEvent = semesterEvent(1L, 4L, "오리엔테이션");
        given(semesterEventRepository.findAllBySemesterIdAndTypeOrderByIdAsc(4L, EVENT)).willReturn(List.of(semesterEvent));

        // when
        SemesterEventsResponse response = adminSemesterEventService.getEvents(4L, EVENT);

        // then
        assertThat(response.type()).isEqualTo(EVENT);
        assertThat(response.events()).hasSize(1);
        assertThat(response.events().getFirst().id()).isEqualTo(1L);
        assertThat(response.events().getFirst().name()).isEqualTo("오리엔테이션");
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 행사 목록을 반환한다")
    void 조회_결과가_없으면_빈_행사_목록을_반환한다() {
        // given
        given(semesterEventRepository.findAllBySemesterIdAndTypeOrderByIdAsc(4L, EVENT)).willReturn(List.of());

        // when
        SemesterEventsResponse response = adminSemesterEventService.getEvents(4L, EVENT);

        // then
        assertThat(response.type()).isEqualTo(EVENT);
        assertThat(response.events()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 기수의 행사 목록을 조회하면 예외가 발생한다")
    void 존재하지_않는_기수의_행사_목록을_조회하면_예외가_발생한다() {
        // given
        given(semesterRepository.existsById(4L)).willReturn(false);

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.getEvents(4L, EVENT));

        // then
        assertSemesterError(throwable, SemesterErrorCode.NOT_FOUND_SEMESTER);
    }

    private SemesterEvent semesterEvent(Long id, Long semesterId, String name) {
        SemesterEvent semesterEvent = SemesterEvent.create(semesterId, EVENT, name);
        ReflectionTestUtils.setField(semesterEvent, "id", id);
        return semesterEvent;
    }

    private void assertSemesterError(Throwable throwable, SemesterErrorCode errorCode) {
        assertThat(throwable)
                .isInstanceOf(SemesterException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
