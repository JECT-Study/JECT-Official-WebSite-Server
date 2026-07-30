package org.ject.support.admin.semester.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.ject.support.domain.recruit.domain.SemesterEventType.EVENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.ject.support.admin.semester.dto.CreateSemesterEventRequest;
import org.ject.support.admin.semester.dto.EditSemesterEventsRequest;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.admin.semester.dto.UpdateSemesterEventRequest;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.SemesterEventRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class AdminSemesterEventServiceTest extends UnitTestSupport {

    private static final long MAX_EVENT_COUNT = 10L;

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

    @Test
    @DisplayName("선택한 행사 유형으로 신규 행사를 추가한다")
    void 선택한_행사_유형으로_신규_행사를_추가한다() {
        // given
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, List.of(new CreateSemesterEventRequest("오리엔테이션")), null);

        // when
        adminSemesterEventService.editEvents(4L, request);

        // then
        SemesterEvent savedEvent = captureSavedEvents().getFirst();
        assertThat(savedEvent.getSemesterId()).isEqualTo(4L);
        assertThat(savedEvent.getType()).isEqualTo(EVENT);
        assertThat(savedEvent.getName()).isEqualTo("오리엔테이션");
        assertThat(savedEvent.getIsRequired()).isTrue();
    }

    @Test
    @DisplayName("행사는 유형별 등록 가능한 최대 개수까지 추가한다")
    void 행사는_유형별_등록_가능한_최대_개수까지_추가한다() {
        // given
        given(semesterEventRepository.countBySemesterIdAndType(4L, EVENT)).willReturn(MAX_EVENT_COUNT - 1);
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, List.of(new CreateSemesterEventRequest("최종 발표")), null);

        // when
        adminSemesterEventService.editEvents(4L, request);

        // then
        assertThat(captureSavedEvents()).hasSize(1);
    }

    @Test
    @DisplayName("행사가 유형별 등록 가능한 최대 개수를 초과하면 예외가 발생한다")
    void 행사가_유형별_등록_가능한_최대_개수를_초과하면_예외가_발생한다() {
        // given
        given(semesterEventRepository.countBySemesterIdAndType(4L, EVENT)).willReturn(MAX_EVENT_COUNT);
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, List.of(new CreateSemesterEventRequest("최종 발표")), null);

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.editEvents(4L, request));

        // then
        assertSemesterError(throwable, SemesterErrorCode.EXCEEDED_SEMESTER_EVENT_LIMIT);
        verify(semesterEventRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("기존 기수 행사 이름을 수정한다")
    void 기존_기수_행사_이름을_수정한다() {
        // given
        SemesterEvent semesterEvent = semesterEvent(1L, 4L, "오리엔테이션");
        given(semesterEventRepository.findAllByIdInAndSemesterIdAndType(List.of(1L), 4L, EVENT)).willReturn(List.of(semesterEvent));
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, null, List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션")));

        // when
        adminSemesterEventService.editEvents(4L, request);

        // then
        assertThat(semesterEvent.getName()).isEqualTo("사전 오리엔테이션");
    }

    @Test
    @DisplayName("행사가 최대 개수만큼 등록되어 있어도 기존 행사 이름을 수정한다")
    void 행사가_최대_개수만큼_등록되어_있어도_기존_행사_이름을_수정한다() {
        // given
        SemesterEvent semesterEvent = semesterEvent(1L, 4L, "오리엔테이션");
        given(semesterEventRepository.findAllByIdInAndSemesterIdAndType(List.of(1L), 4L, EVENT)).willReturn(List.of(semesterEvent));
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, null, List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션")));

        // when
        adminSemesterEventService.editEvents(4L, request);

        // then
        assertThat(semesterEvent.getName()).isEqualTo("사전 오리엔테이션");
        verify(semesterEventRepository, never()).countBySemesterIdAndType(anyLong(), any());
    }

    @Test
    @DisplayName("같은 행사를 두 번 수정하면 예외가 발생한다")
    void 같은_행사를_두_번_수정하면_예외가_발생한다() {
        // given
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(
                EVENT,
                null,
                List.of(
                        new UpdateSemesterEventRequest(1L, "사전 오리엔테이션"),
                        new UpdateSemesterEventRequest(1L, "최종 오리엔테이션")
                )
        );

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.editEvents(4L, request));

        // then
        assertSemesterError(throwable, SemesterErrorCode.DUPLICATED_SEMESTER_EVENT_ID);
    }

    @Test
    @DisplayName("선택한 기수와 행사 유형에 없는 행사를 수정하면 예외가 발생한다")
    void 선택한_기수와_행사_유형에_없는_행사를_수정하면_예외가_발생한다() {
        // given
        given(semesterEventRepository.findAllByIdInAndSemesterIdAndType(List.of(1L), 4L, EVENT)).willReturn(List.of());
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, null, List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션")));

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.editEvents(4L, request));

        // then
        assertSemesterError(throwable, SemesterErrorCode.NOT_FOUND_SEMESTER_EVENT);
    }

    @Test
    @DisplayName("신규 행사 추가와 기존 행사 이름 수정을 함께 반영한다")
    void 신규_행사_추가와_기존_행사_이름_수정을_함께_반영한다() {
        // given
        SemesterEvent semesterEvent = semesterEvent(1L, 4L, "오리엔테이션");
        given(semesterEventRepository.findAllByIdInAndSemesterIdAndType(List.of(1L), 4L, EVENT)).willReturn(List.of(semesterEvent));
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(
                EVENT,
                List.of(new CreateSemesterEventRequest("최종 발표")),
                List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션"))
        );

        // when
        adminSemesterEventService.editEvents(4L, request);

        // then
        assertThat(captureSavedEvents().getFirst().getName()).isEqualTo("최종 발표");
        assertThat(semesterEvent.getName()).isEqualTo("사전 오리엔테이션");
    }

    @Test
    @DisplayName("추가 가능한 개수를 초과하면 함께 요청한 이름 수정도 반영하지 않는다")
    void 추가_가능한_개수를_초과하면_함께_요청한_이름_수정도_반영하지_않는다() {
        // given
        SemesterEvent semesterEvent = semesterEvent(1L, 4L, "오리엔테이션");
        given(semesterEventRepository.countBySemesterIdAndType(4L, EVENT))
                .willReturn(MAX_EVENT_COUNT);
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(
                EVENT,
                List.of(new CreateSemesterEventRequest("최종 발표")),
                List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션"))
        );

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.editEvents(4L, request));

        // then
        assertSemesterError(throwable, SemesterErrorCode.EXCEEDED_SEMESTER_EVENT_LIMIT);
        assertThat(semesterEvent.getName()).isEqualTo("오리엔테이션");
        verify(semesterEventRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("잘못된 수정 대상이 포함되면 어떤 행사도 변경하지 않는다")
    void 잘못된_수정_대상이_포함되면_어떤_행사도_변경하지_않는다() {
        // given
        SemesterEvent semesterEvent = semesterEvent(1L, 4L, "오리엔테이션");
        given(semesterEventRepository.findAllByIdInAndSemesterIdAndType(
                List.of(1L, 2L), 4L, EVENT))
                .willReturn(List.of(semesterEvent));
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(
                EVENT,
                List.of(new CreateSemesterEventRequest("최종 발표")),
                List.of(
                        new UpdateSemesterEventRequest(1L, "사전 오리엔테이션"),
                        new UpdateSemesterEventRequest(2L, "존재하지 않는 행사")
                )
        );

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.editEvents(4L, request));

        // then
        assertSemesterError(throwable, SemesterErrorCode.NOT_FOUND_SEMESTER_EVENT);
        assertThat(semesterEvent.getName()).isEqualTo("오리엔테이션");
        verify(semesterEventRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("존재하지 않는 기수에 행사를 추가하거나 수정하면 예외가 발생한다")
    void 존재하지_않는_기수에_행사를_추가하거나_수정하면_예외가_발생한다() {
        // given
        given(semesterRepository.existsById(4L)).willReturn(false);
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(
                EVENT,
                List.of(new CreateSemesterEventRequest("최종 발표")),
                List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션"))
        );

        // when
        Throwable throwable = catchThrowable(() -> adminSemesterEventService.editEvents(4L, request));

        // then
        assertSemesterError(throwable, SemesterErrorCode.NOT_FOUND_SEMESTER);
        verify(semesterEventRepository, never()).saveAll(any());
    }

    private SemesterEvent semesterEvent(Long id, Long semesterId, String name) {
        SemesterEvent semesterEvent = SemesterEvent.create(semesterId, EVENT, name);
        ReflectionTestUtils.setField(semesterEvent, "id", id);
        return semesterEvent;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<SemesterEvent> captureSavedEvents() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(semesterEventRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private void assertSemesterError(Throwable throwable, SemesterErrorCode errorCode) {
        assertThat(throwable)
                .isInstanceOf(SemesterException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
