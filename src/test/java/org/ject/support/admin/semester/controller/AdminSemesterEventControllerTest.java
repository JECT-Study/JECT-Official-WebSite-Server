package org.ject.support.admin.semester.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.recruit.domain.SemesterEventType.EVENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ject.support.admin.semester.dto.CreateSemesterEventRequest;
import org.ject.support.admin.semester.dto.EditSemesterEventsRequest;
import org.ject.support.admin.semester.dto.SemesterEventResponse;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.admin.semester.dto.UpdateSemesterEventRequest;
import org.ject.support.admin.semester.service.AdminSemesterEventService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminSemesterEventControllerTest extends UnitTestSupport {

    @Mock
    private AdminSemesterEventService adminSemesterEventService;

    @InjectMocks
    private AdminSemesterEventController adminSemesterEventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(adminSemesterEventController)
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .build();
    }

    @Test
    @DisplayName("기수와 행사 유형으로 행사 목록을 조회한다")
    void 기수와_행사_유형으로_행사_목록을_조회한다() throws Exception {
        // given
        SemesterEventsResponse response = new SemesterEventsResponse(EVENT, List.of(new SemesterEventResponse(1L, "오리엔테이션")));
        given(adminSemesterEventService.getEvents(4L, EVENT)).willReturn(response);

        // when, then
        mockMvc.perform(get("/admin/semesters/{semesterId}/events", 4L)
                        .param("type", "EVENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.type").value("EVENT"))
                .andExpect(jsonPath("$.data.events[0].id").value(1L))
                .andExpect(jsonPath("$.data.events[0].name").value("오리엔테이션"));
    }

    @Test
    @DisplayName("행사 유형이 없으면 행사 목록을 조회하지 않는다")
    void 행사_유형이_없으면_행사_목록을_조회하지_않는다() throws Exception {
        // when, then
        mockMvc.perform(get("/admin/semesters/{semesterId}/events", 4L))
                .andExpect(status().isBadRequest());

        verify(adminSemesterEventService, never()).getEvents(any(), any());
    }

    @Test
    @DisplayName("기수 행사의 추가와 수정을 함께 요청한다")
    void 기수_행사의_추가와_수정을_함께_요청한다() throws Exception {
        // given
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(
                EVENT,
                List.of(new CreateSemesterEventRequest("최종 발표")),
                List.of(new UpdateSemesterEventRequest(1L, "사전 오리엔테이션"))
        );

        // when, then
        mockMvc.perform(post("/admin/semesters/{semesterId}/events", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        ArgumentCaptor<EditSemesterEventsRequest> captor = ArgumentCaptor.forClass(EditSemesterEventsRequest.class);
        verify(adminSemesterEventService).editEvents(eq(4L), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(EVENT);
        assertThat(captor.getValue().created().getFirst().name()).isEqualTo("최종 발표");
        assertThat(captor.getValue().updated().getFirst().name()).isEqualTo("사전 오리엔테이션");
    }

    @Test
    @DisplayName("추가와 수정 목록이 모두 없으면 기수 행사를 변경하지 않는다")
    void 추가와_수정_목록이_모두_없으면_기수_행사를_변경하지_않는다() throws Exception {
        // given
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, null, null);

        // when, then
        mockMvc.perform(post("/admin/semesters/{semesterId}/events", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminSemesterEventService, never()).editEvents(any(), any());
    }

    @Test
    @DisplayName("변경 목록이 비어 있으면 기수 행사를 변경하지 않는다")
    void 변경_목록이_비어_있으면_기수_행사를_변경하지_않는다() throws Exception {
        // given
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, List.of(), null);

        // when, then
        mockMvc.perform(post("/admin/semesters/{semesterId}/events", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminSemesterEventService, never()).editEvents(any(), any());
    }

    @Test
    @DisplayName("필수값이 없으면 기수 행사를 변경하지 않는다")
    void 필수값이_없으면_기수_행사를_변경하지_않는다() throws Exception {
        // given
        EditSemesterEventsRequest request = new EditSemesterEventsRequest(EVENT, List.of(new CreateSemesterEventRequest(" ")), null);

        // when, then
        mockMvc.perform(post("/admin/semesters/{semesterId}/events", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminSemesterEventService, never()).editEvents(any(), any());
    }
}
