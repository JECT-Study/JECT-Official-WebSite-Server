package org.ject.support.admin.mail.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.ject.support.admin.mail.dto.MailRecruitResponse;
import org.ject.support.admin.mail.service.MailRecruitService;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
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
class AdminMailRecruitSecurityTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MailRecruitService mailRecruitService;

    @Test
    void 인증되지_않은_사용자는_메일_발송용_모집_공고를_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/mails/recruits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = false)
    void 일반_사용자는_메일_발송용_모집_공고를_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/mails/recruits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @AuthenticatedUser(isAdmin = true)
    void 관리자는_메일_발송용_모집_공고를_조회할_수_있다() throws Exception {
        given(mailRecruitService.getRecruits()).willReturn(List.of(
                new MailRecruitResponse(
                        1L, 2L, "10기", JobFamily.BE, JobFamily.BE.getDescription(),
                        RecruitType.SEMESTER, RecruitType.SEMESTER.getDescription(),
                        RecruitTypeDetail.REGULAR, RecruitTypeDetail.REGULAR.getDescription(),
                        null, null, null)));

        mockMvc.perform(get("/admin/mails/recruits"))
                .andExpect(status().isOk());
    }
}
