package org.ject.support.admin.mail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.service.MailPreviewService;
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
class AdminMailPreviewSecurityTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MailPreviewService mailPreviewService;

    @Test
    @DisplayName("인증되지 않은 사용자는 메일 미리보기를 조회할 수 없다")
    void 인증되지_않은_사용자는_메일_미리보기를_조회할_수_없다() throws Exception {
        mockMvc.perform(post("/admin/mails/scenarios/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioId\":1,\"applyId\":20,\"inputVariables\":{}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = false)
    @DisplayName("일반 사용자는 메일 미리보기를 조회할 수 없다")
    void 일반_사용자는_메일_미리보기를_조회할_수_없다() throws Exception {
        mockMvc.perform(post("/admin/mails/scenarios/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioId\":1,\"applyId\":20,\"inputVariables\":{}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = true)
    @DisplayName("관리자는 메일 미리보기를 조회할 수 있다")
    void 관리자는_메일_미리보기를_조회할_수_있다() throws Exception {
        given(mailPreviewService.preview(any())).willReturn(
                new MailPreviewResponse(1L, 20L, "applicant@ject.kr", "제목", "본문"));

        mockMvc.perform(post("/admin/mails/scenarios/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioId\":1,\"applyId\":20,\"inputVariables\":{}}"))
                .andExpect(status().isOk());
    }
}
