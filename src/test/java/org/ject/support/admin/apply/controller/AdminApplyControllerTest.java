package org.ject.support.admin.apply.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminApplyControllerTest extends UnitTestSupport {

    @InjectMocks
    private AdminApplyController adminApplyController;

    @Mock
    private AdminApplyService adminApplyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminApplyController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new org.ject.support.common.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void 지원서_목록_조회시_공고_필터를_전달한다() throws Exception {
        // given
        var condition = new AdminApplySearchCondition(
                ApplyStatus.SUBMITTED, 1L, JobFamily.BE, RecruitType.SEMESTER, RecruitTypeDetail.REFILL, 10L);
        var pageable = PageRequest.of(0, 15);
        var page = new PageImpl<AdminApplyResponse>(List.of(), pageable, 0);

        given(adminApplyService.findApplies(eq(condition), any(Pageable.class)))
                .willReturn(page);

        // when, then
        mockMvc.perform(get("/admin/applies")
                        .param("applyStatus", "SUBMITTED")
                        .param("semesterId", "1")
                        .param("jobFamily", "BE")
                        .param("recruitType", "SEMESTER")
                        .param("recruitTypeDetail", "REFILL")
                        .param("recruitId", "10")
                        .param("page", "0")
                        .param("size", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminApplyService).findApplies(eq(condition), any(Pageable.class));
    }

    @Test
    void 선정_결과_일괄_변경_요청을_처리한다() throws Exception {
        // given
        given(adminApplyService.updateSelectionResults(any())).willReturn(1);

        // when, then
        mockMvc.perform(patch("/admin/applies/selection-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recruitId": 1,
                                  "selectionResults": [
                                    {"applyId": 1, "selectionResult": "PASSED", "waitlistNumber": null}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(adminApplyService).updateSelectionResults(any());
    }
}
