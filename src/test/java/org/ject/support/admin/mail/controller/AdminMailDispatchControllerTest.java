package org.ject.support.admin.mail.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.admin.mail.controller.AdminMailDispatchController;
import org.ject.support.admin.mail.dto.MailDispatchDetailResponse;
import org.ject.support.admin.mail.dto.MailDispatchExecuteResponse;
import org.ject.support.admin.mail.dto.MailDispatchFailedTargetResponse;
import org.ject.support.admin.mail.dto.MailDispatchHistoryResponse;
import org.ject.support.admin.mail.dto.MailDispatchRequest;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.MailPreviewRequest;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.MailTestSendRequest;
import org.ject.support.admin.mail.dto.MailTestSendResponse;
import org.ject.support.admin.mail.service.MailDispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMailDispatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminMailDispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MailDispatchService mailDispatchService;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtExceptionHandlerFilter jwtExceptionHandlerFilter;

    @Test
    @DisplayName("메일 미리보기를 정상적으로 반환한다")
    void preview() throws Exception {
        MailPreviewRequest request = new MailPreviewRequest(
                1L,
                10L,
                Map.of("RECRUIT_NAME", "메이커스")
        );
        MailPreviewResponse response = new MailPreviewResponse(
                1L,
                10L,
                "member@ject.kr",
                "[JECT] 메이커스 결과",
                "안녕하세요 젝트님"
        );
        given(mailDispatchService.preview(any(MailPreviewRequest.class))).willReturn(response);

        mockMvc.perform(post("/admin/mail-dispatches/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mailScenarioId").value(1L))
                .andExpect(jsonPath("$.data.receiverId").value(10L))
                .andExpect(jsonPath("$.data.subject").value("[JECT] 메이커스 결과"));
    }

    @Test
    @DisplayName("테스트 메일 발송을 정상적으로 처리한다")
    void testSend() throws Exception {
        MailTestSendRequest request = new MailTestSendRequest(
                1L,
                10L,
                "test@ject.kr",
                Map.of("RECRUIT_NAME", "메이커스")
        );
        MailTestSendResponse response = new MailTestSendResponse(
                1L,
                10L,
                "test@ject.kr",
                "[JECT] 메이커스 결과",
                "SENT"
        );
        given(mailDispatchService.sendTestMail(any(MailTestSendRequest.class))).willReturn(response);

        mockMvc.perform(post("/admin/mail-dispatches/test-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mailScenarioId").value(1L))
                .andExpect(jsonPath("$.data.receiverId").value(10L))
                .andExpect(jsonPath("$.data.toEmail").value("test@ject.kr"))
                .andExpect(jsonPath("$.data.status").value("SENT"));
    }

    @Test
    @DisplayName("메일 발송 작업 생성을 정상적으로 처리한다")
    void dispatch() throws Exception {
        MailDispatchRequest request = new MailDispatchRequest(
                1L,
                List.of(10L, 11L),
                Map.of("RECRUIT_NAME", "메이커스")
        );
        MailDispatchResponse response = new MailDispatchResponse(100L, 1L, "REQUESTED", 2);
        given(mailDispatchService.dispatch(any(MailDispatchRequest.class))).willReturn(response);

        mockMvc.perform(post("/admin/mail-dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dispatchJobId").value(100L))
                .andExpect(jsonPath("$.data.receiverCount").value(2));
    }

    @Test
    @DisplayName("메일 발송 작업 실행을 정상적으로 처리한다")
    void executeDispatch() throws Exception {
        MailDispatchExecuteResponse response = new MailDispatchExecuteResponse(100L, "COMPLETED", 2, 0);
        given(mailDispatchService.executeDispatch(100L)).willReturn(response);

        mockMvc.perform(post("/admin/mail-dispatches/{dispatchJobId}/execute", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dispatchJobId").value(100L))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failedCount").value(0));
    }

    @Test
    @DisplayName("메일 발송 작업 이력 목록을 정상적으로 조회한다")
    void getDispatchHistories() throws Exception {
        List<MailDispatchHistoryResponse> response = List.of(
                new MailDispatchHistoryResponse(
                        101L,
                        1L,
                        "MEMBER_REJECT_NOTICE",
                        "일반 구성원 - 불합격 통지",
                        "FAILED",
                        2,
                        1,
                        1,
                        LocalDateTime.of(2026, 4, 9, 10, 0),
                        LocalDateTime.of(2026, 4, 9, 10, 1),
                        LocalDateTime.of(2026, 4, 9, 10, 2)
                )
        );
        given(mailDispatchService.getDispatchHistories()).willReturn(response);

        mockMvc.perform(get("/admin/mail-dispatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].dispatchJobId").value(101L))
                .andExpect(jsonPath("$.data[0].scenarioCode").value("MEMBER_REJECT_NOTICE"))
                .andExpect(jsonPath("$.data[0].failedCount").value(1));
    }

    @Test
    @DisplayName("메일 발송 작업 상세를 정상적으로 조회한다")
    void getDispatchHistory() throws Exception {
        MailDispatchDetailResponse response = new MailDispatchDetailResponse(
                101L,
                1L,
                "MEMBER_REJECT_NOTICE",
                "일반 구성원 - 불합격 통지",
                "FAILED",
                2,
                0,
                1,
                1,
                Map.of("RECRUIT_NAME", "메이커스"),
                LocalDateTime.of(2026, 4, 9, 10, 0),
                LocalDateTime.of(2026, 4, 9, 10, 1),
                LocalDateTime.of(2026, 4, 9, 10, 2)
        );
        given(mailDispatchService.getDispatchHistory(101L)).willReturn(response);

        mockMvc.perform(get("/admin/mail-dispatches/{dispatchJobId}", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dispatchJobId").value(101L))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.sentCount").value(1))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.commonVariables.RECRUIT_NAME").value("메이커스"));
    }

    @Test
    @DisplayName("메일 발송 실패 대상을 정상적으로 조회한다")
    void getFailedTargets() throws Exception {
        List<MailDispatchFailedTargetResponse> response = List.of(
                new MailDispatchFailedTargetResponse(
                        301L,
                        11L,
                        "fail@ject.kr",
                        "ses timeout",
                        LocalDateTime.of(2026, 4, 9, 10, 2)
                )
        );
        given(mailDispatchService.getFailedTargets(101L)).willReturn(response);

        mockMvc.perform(get("/admin/mail-dispatches/{dispatchJobId}/failed-targets", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].targetId").value(301L))
                .andExpect(jsonPath("$.data[0].email").value("fail@ject.kr"))
                .andExpect(jsonPath("$.data[0].failureReason").value("ses timeout"));
    }
}
