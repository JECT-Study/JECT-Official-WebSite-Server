package org.ject.support.admin.mail.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.service.MailTargetService;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.ject.support.testconfig.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class AdminMailTargetSecurityTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MailTargetService mailTargetService;

    @Test
    void 인증되지_않은_사용자는_메일_발송_대상을_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/mails/targets").param("recruitId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = false)
    void 일반_사용자는_메일_발송_대상을_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/mails/targets").param("recruitId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = true)
    void 관리자는_메일_발송_대상을_조회할_수_있다() throws Exception {
        given(mailTargetService.searchTargets(1L, null)).willReturn(List.of(
                new MailTargetResponse(1L, "홍길동", "01012345678", "test@test.com", SelectionResult.PASSED, null)));

        mockMvc.perform(get("/admin/mails/targets").param("recruitId", "1"))
                .andExpect(status().isOk());
    }
}
