package org.ject.support.admin.apply.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
                .setControllerAdvice(new org.ject.support.common.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("제출완료 지원서 목록 조회 조건을 전달한다")
    void 제출완료_지원서_목록_조회조건을_전달한다() throws Exception {
        // given
        var condition = new SubmittedApplySearchCondition(
                10L,
                Set.of(CareerDetails.STUDENT, CareerDetails.EMPLOYEE),
                Set.of(SelectionResult.PASSED, SelectionResult.WAITLISTED),
                100L,
                20);
        given(adminApplyService.searchSubmittedApplies(condition))
                .willReturn(CursorPageResponse.of(List.of(), 20, false, null, 0L));

        // when, then
        mockMvc.perform(get("/admin/applies/submitted")
                        .param("recruitId", "10")
                        .param("careerDetails", "STUDENT", "EMPLOYEE")
                        .param("selectionResults", "PASSED", "WAITLISTED")
                        .param("cursor", "100")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminApplyService).searchSubmittedApplies(condition);
    }

    @Test
    @DisplayName("임시저장 지원서 목록 조회 조건을 전달한다")
    void 임시저장_지원서_목록_조회조건을_전달한다() throws Exception {
        // given
        var condition = new TempSavedApplySearchCondition(
                10L,
                Set.of(CareerDetails.STUDENT, CareerDetails.EMPLOYEE),
                100L,
                20);
        given(adminApplyService.searchTempSavedApplies(condition))
                .willReturn(CursorPageResponse.of(List.of(), 20, false, null, 0L));

        // when, then
        mockMvc.perform(get("/admin/applies/temp-saved")
                        .param("recruitId", "10")
                        .param("careerDetails", "STUDENT", "EMPLOYEE")
                        .param("cursor", "100")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminApplyService).searchTempSavedApplies(condition);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/applies/submitted", "/admin/applies/temp-saved"})
    @DisplayName("공고 ID가 없으면 지원서 목록 조회에 실패한다")
    void 공고_ID가_없으면_지원서_목록_조회에_실패한다(String path) throws Exception {
        mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
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
