package org.ject.support.admin.mail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.service.MailDispatchUseCase;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.ject.support.testconfig.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class AdminMailDispatchSecurityTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MailDispatchUseCase mailDispatchUseCase;

    @Test
    @DisplayName("인증되지 않은 사용자는 단체 메일을 발송할 수 없다")
    void 인증되지_않은_사용자는_단체_메일을_발송할_수_없다() throws Exception {
        mockMvc.perform(post("/admin/mails/dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "dispatch-key")
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = false)
    @DisplayName("일반 사용자는 단체 메일을 발송할 수 없다")
    void 일반_사용자는_단체_메일을_발송할_수_없다() throws Exception {
        mockMvc.perform(post("/admin/mails/dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "dispatch-key")
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = true)
    @DisplayName("관리자는 단체 메일을 발송할 수 있다")
    void 관리자는_단체_메일을_발송할_수_있다() throws Exception {
        given(mailDispatchUseCase.sendMail(any(), any(), any())).willReturn(
                new MailDispatchResponse(100L, MailDispatchJobStatus.COMPLETED, 1, 0, 1, 0));

        mockMvc.perform(post("/admin/mails/dispatches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "dispatch-key")
                        .content(validRequest()))
                .andExpect(status().isCreated());
    }

    private String validRequest() {
        return "{\"recruitId\":2,\"scenarioId\":1,\"applyIds\":[10],\"inputVariables\":{}}";
    }
}
