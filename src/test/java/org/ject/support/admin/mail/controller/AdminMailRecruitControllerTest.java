package org.ject.support.admin.mail.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.dto.MailRecruitResponse;
import org.ject.support.admin.mail.service.MailRecruitService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

class AdminMailRecruitControllerTest extends UnitTestSupport {

    private MockMvc mockMvc;

    private final OptionalValidatorFactoryBean validator = new OptionalValidatorFactoryBean();

    @Mock
    private MailRecruitService mailRecruitService;

    @InjectMocks
    private AdminMailRecruitController adminMailRecruitController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminMailRecruitController)
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .setValidator(validator)
                .build();
    }

    @Test
    void 메일_발송용_모집_공고_목록을_조회한다() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 8, 31, 23, 59);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 1, 10, 0);
        MailRecruitResponse response = new MailRecruitResponse(
                1L,
                2L,
                "10기",
                JobFamily.BE,
                JobFamily.BE.getDescription(),
                RecruitType.SEMESTER,
                RecruitType.SEMESTER.getDescription(),
                RecruitTypeDetail.REGULAR,
                RecruitTypeDetail.REGULAR.getDescription(),
                startDate,
                endDate,
                createdAt);
        given(mailRecruitService.getRecruits()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/mails/recruits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].recruitId").value(1))
                .andExpect(jsonPath("$.data[0].semesterId").value(2))
                .andExpect(jsonPath("$.data[0].semesterName").value("10기"))
                .andExpect(jsonPath("$.data[0].jobFamily").value("BE"))
                .andExpect(jsonPath("$.data[0].jobFamilyDescription").value("백엔드 개발자(BE)"))
                .andExpect(jsonPath("$.data[0].recruitType").value("SEMESTER"))
                .andExpect(jsonPath("$.data[0].recruitTypeDescription").value("정규 기수 모집"))
                .andExpect(jsonPath("$.data[0].recruitTypeDetail").value("REGULAR"))
                .andExpect(jsonPath("$.data[0].recruitTypeDetailDescription").value("정규 모집"))
                .andExpect(jsonPath("$.data[0].startDate[0]").value(2026))
                .andExpect(jsonPath("$.data[0].startDate[1]").value(8))
                .andExpect(jsonPath("$.data[0].startDate[2]").value(1))
                .andExpect(jsonPath("$.data[0].endDate[0]").value(2026))
                .andExpect(jsonPath("$.data[0].endDate[2]").value(31))
                .andExpect(jsonPath("$.data[0].createdAt[0]").value(2026))
                .andExpect(jsonPath("$.data[0].createdAt[2]").value(1));
        then(mailRecruitService).should().getRecruits();
    }

    @Test
    void 모집_공고가_없으면_빈_목록을_반환한다() throws Exception {
        // given
        given(mailRecruitService.getRecruits()).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/admin/mails/recruits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
