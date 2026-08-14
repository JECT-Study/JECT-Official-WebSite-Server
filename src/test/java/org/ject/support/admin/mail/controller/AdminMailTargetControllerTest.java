package org.ject.support.admin.mail.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.dto.MailTargetSelectionResult;
import org.ject.support.admin.mail.service.MailTargetService;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminMailTargetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MailTargetService mailTargetService;

    @InjectMocks
    private AdminMailTargetController adminMailTargetController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminMailTargetController)
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .build();
    }

    @Test
    void 선정_결과를_지정해_메일_발송_대상을_조회한다() throws Exception {
        // given
        MailTargetResponse response = new MailTargetResponse(
                10L, "홍길동", "01012345678", "test@test.com", SelectionResult.WAITLISTED, 2);
        given(mailTargetService.getTargets(1L, SelectionResult.WAITLISTED)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/mails/targets")
                        .param("recruitId", "1")
                        .param("selectionResult", "WAITLISTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].applyId").value(10))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data[0].phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.data[0].email").value("test@test.com"))
                .andExpect(jsonPath("$.data[0].selectionResult").value("WAITLISTED"))
                .andExpect(jsonPath("$.data[0].waitlistNumber").value(2));
        then(mailTargetService).should().getTargets(1L, SelectionResult.WAITLISTED);
    }

    @Test
    void 선정_결과_없이_전체_메일_발송_대상을_조회한다() throws Exception {
        // given
        given(mailTargetService.getTargets(1L, null)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/admin/mails/targets").param("recruitId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        then(mailTargetService).should().getTargets(1L, null);
    }

    @Test
    void 미지원_선정_결과는_400을_반환한다() throws Exception {
        mockMvc.perform(get("/admin/mails/targets")
                        .param("recruitId", "1")
                        .param("selectionResult", "UNDECIDED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_모집_공고면_404를_반환한다() throws Exception {
        // given
        given(mailTargetService.getTargets(999L, null))
                .willThrow(new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));

        // when & then
        mockMvc.perform(get("/admin/mails/targets").param("recruitId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("RECRUIT-1"));
    }
}
