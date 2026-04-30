package org.ject.support.domain.recruit.controller;

import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponse;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.service.RecruitUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.recruit.domain.RecruitType.MAKERS;
import static org.ject.support.domain.recruit.domain.RecruitType.SEMESTER;
import static org.ject.support.domain.recruit.domain.RecruitTypeDetail.NEW;
import static org.ject.support.domain.recruit.domain.RecruitTypeDetail.REGULAR;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecruitControllerTest {

    MockMvc mockMvc;

    @Mock
    RecruitUsecase recruitUsecase;

    @InjectMocks
    RecruitController recruitController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recruitController)
                .setControllerAdvice(new ResponseWrapper())
                .build();
    }

    @Test
    void 활성_모집_공고_목록을_조회한다() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        ActiveRecruitmentResponses responses = new ActiveRecruitmentResponses(List.of(
                new ActiveRecruitmentResponse(
                        1L, 3L, "3기", SEMESTER, "정규 기수 모집", REGULAR, "정규 모집",
                        BE, "백엔드 개발자(BE)", now.minusDays(2), now.plusDays(2)),
                new ActiveRecruitmentResponse(
                        2L, 3L, "3기", MAKERS, "메이커스 모집", NEW, "신규 모집",
                        FE, "프론트엔드 개발자(FE)", now.minusDays(1), now.plusDays(2))
        ));
        given(recruitUsecase.findActiveRecruitments()).willReturn(responses);

        // when, then
        mockMvc.perform(get("/recruit/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recruitments", hasSize(2)))
                .andExpect(jsonPath("$.data.recruitments[0].recruitType").value("SEMESTER"))
                .andExpect(jsonPath("$.data.recruitments[0].recruitTypeDescription").value("정규 기수 모집"))
                .andExpect(jsonPath("$.data.recruitments[0].recruitTypeDetail").value("REGULAR"))
                .andExpect(jsonPath("$.data.recruitments[0].jobFamily").value("BE"))
                .andExpect(jsonPath("$.data.recruitments[1].recruitType").value("MAKERS"))
                .andExpect(jsonPath("$.data.recruitments[1].recruitTypeDetail").value("NEW"))
                .andExpect(jsonPath("$.data.recruitments[1].jobFamily").value("FE"));
        verify(recruitUsecase).findActiveRecruitments();
    }

    @Test
    void 활성_모집_공고가_없으면_빈_목록을_반환한다() throws Exception {
        // given
        given(recruitUsecase.findActiveRecruitments()).willReturn(new ActiveRecruitmentResponses(List.of()));

        // when, then
        mockMvc.perform(get("/recruit/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recruitments", hasSize(0)));
    }
}
