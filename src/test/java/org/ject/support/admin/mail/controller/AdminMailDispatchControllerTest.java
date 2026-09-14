package org.ject.support.admin.mail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.service.MailDispatchUseCase;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.common.security.AuthenticatedApplicantIdResolver;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminMailDispatchControllerTest extends UnitTestSupport {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private MailDispatchUseCase mailDispatchUseCase;

    @InjectMocks
    private AdminMailDispatchController adminMailDispatchController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminMailDispatchController)
                .setCustomArgumentResolvers(new AuthenticatedApplicantIdResolver())
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .build();
        setAuthentication();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("메일 발송 요청을 받아 결과를 반환한다")
    void 메일_발송_요청을_받아_결과를_반환한다() throws Exception {
        // given
        SendMailDispatchRequest request = new SendMailDispatchRequest(2L, 1L, List.of(10L), null, Map.of());
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 1, 0, 1, 0);
        given(mailDispatchUseCase.sendMail(
                any(SendMailDispatchRequest.class), eq(50L), eq("dispatch-key"))).willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/mails/dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "dispatch-key")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dispatchJobId").value(100))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.successCount").value(1));

        verify(mailDispatchUseCase).sendMail(request, 50L, "dispatch-key");
    }

    @Test
    @DisplayName("Idempotency-Key가 없으면 메일 발송 요청을 거부한다")
    void Idempotency_Key가_없으면_메일_발송_요청을_거부한다() throws Exception {
        // given
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, List.of(10L), null, Map.of());

        // when & then
        mockMvc.perform(post("/admin/mails/dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("발송 대상이 501개면 요청을 거부한다")
    void 발송_대상이_501개면_요청을_거부한다() throws Exception {
        // given
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, java.util.stream.LongStream.rangeClosed(1, 501).boxed().toList(), null, Map.of());

        // when & then
        mockMvc.perform(post("/admin/mails/dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "dispatch-key")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private void setAuthentication() {
        CustomUserDetails userDetails = new CustomUserDetails("admin@ject.kr", 50L, Role.ADMIN);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
