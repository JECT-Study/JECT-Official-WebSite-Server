package org.ject.support.admin.apply.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.EditApplyRequest;
import org.ject.support.admin.apply.dto.EditWaitlistOrderRequest;
import org.ject.support.admin.apply.dto.WaitlistOrderResponse;
import org.ject.support.admin.apply.dto.WaitlistOrdersResponse;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminApplyControllerTest extends UnitTestSupport {

    @InjectMocks
    private AdminApplyController adminApplyController;

    @Mock
    private AdminApplyService adminApplyService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminApplyController)
                .setControllerAdvice(new org.ject.support.common.exception.GlobalExceptionHandler(), new ResponseWrapper())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
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
    @DisplayName("지원서 상세조회 시 선정 결과와 예비 번호를 반환한다")
    void 지원서_상세조회_시_선정_결과와_예비_번호를_반환한다() throws Exception {
        // given
        var response = new AdminApplyDetailResponse(
                1L,
                "김젝트",
                "01012345678",
                "test@mail.com",
                JobFamily.BE,
                "대학생(재학/휴학)",
                "SEMESTER",
                SelectionResult.WAITLISTED,
                2,
                "활동 의사 확인 필요",
                "서울",
                "경험 없음",
                List.of("AI", "Backend"),
                Map.of(),
                List.of());
        given(adminApplyService.findApply(1L, ApplyStatus.SUBMITTED)).willReturn(response);

        // when, then
        mockMvc.perform(get("/admin/applies/{applyId}", 1L)
                        .param("applyStatus", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectionResult").value("WAITLISTED"))
                .andExpect(jsonPath("$.data.waitlistNumber").value(2));

        verify(adminApplyService).findApply(1L, ApplyStatus.SUBMITTED);
    }

    @Test
    void 선정_결과_일괄_변경_요청을_처리한다() throws Exception {
        // given

        // when, then
        mockMvc.perform(patch("/admin/applies/selection-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recruitId": 1,
                                  "applyIds": [1],
                                  "selectionResult": "PASSED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(adminApplyService).updateSelectionResults(any());
    }
    @Test
    void 지원서_수정_요청을_처리한다() throws Exception {
        // given
        ObjectNode body = editBody();

        // when, then
        mockMvc.perform(put("/admin/applies/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isOk());
        verify(adminApplyService).editApply(1L, objectMapper.treeToValue(body, EditApplyRequest.class));
    }

    @ParameterizedTest(name = "{0}에 {1} 입력")
    @MethodSource("invalidRequiredProfiles")
    void 필수_개인정보가_누락되거나_비어있으면_수정을_거부한다(String field, String value) throws Exception {
        // given
        ObjectNode body = editBody();
        if (value == null) {
            body.remove(field);
        } else {
            body.put(field, value);
        }

        // when, then
        mockMvc.perform(put("/admin/applies/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidRequiredProfiles() {
        return Stream.concat(
                Stream.of("name", "phoneNumber", "email")
                        .flatMap(field -> Stream.of(null, "", " ").map(value -> Arguments.of(field, value))),
                Stream.of(Arguments.of("careerDetails", (String) null)));
    }

    @ParameterizedTest(name = "{0} 허용 범위 초과")
    @MethodSource("invalidEditLimits")
    void 관심_도메인과_비고가_허용_범위를_벗어나면_수정을_거부한다(String field, Object value) throws Exception {
        // given
        ObjectNode body = editBody();
        body.set(field, objectMapper.valueToTree(value));

        // when, then
        mockMvc.perform(put("/admin/applies/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidEditLimits() {
        return Stream.of(Arguments.of("interestedDomains", List.of()),
                Arguments.of("interestedDomains", List.of("GAME", "EDUCATION", "MOBILITY", "UTILITY")),
                Arguments.of("note", "가".repeat(101)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jobFamily", "recruitId", "waitlistNumber"})
    void 수정할_수_없는_항목이_포함되면_요청을_거부한다(String field) throws Exception {
        // given
        ObjectNode body = editBody();
        body.put(field, field.equals("jobFamily") ? "FE" : "2");

        // when, then
        mockMvc.perform(put("/admin/applies/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예비번호_조정은_공고와_지원서_순서를_받는다() throws Exception {
        // given
        var request = new EditWaitlistOrderRequest(1L, List.of(3L, 1L, 2L));
        given(adminApplyService.editWaitlistOrder(request)).willReturn(3);

        // when, then
        mockMvc.perform(patch("/admin/applies/waitlist-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));
        verify(adminApplyService).editWaitlistOrder(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"recruitId\":1,\"applyIds\":[],\"selectionResult\":\"PASSED\"}",
            "{\"applyIds\":[1],\"selectionResult\":\"PASSED\"}",
            "{\"recruitId\":1,\"applyIds\":[null],\"selectionResult\":\"PASSED\"}"
    })
    void 선정결과_지정에_공고나_지원서가_없으면_거부한다(String body) throws Exception {
        // when, then
        mockMvc.perform(patch("/admin/applies/selection-results")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예비번호_조정은_빈_목록을_받을_수_없다() throws Exception {
        // when, then
        mockMvc.perform(patch("/admin/applies/waitlist-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recruitId\":1,\"applyIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예비번호_목록은_전체_배열과_인원수를_반환한다() throws Exception {
        // given
        var first = new WaitlistOrderResponse(30L, "지원자", "01012345678", CareerDetails.STUDENT, 1);
        var second = new WaitlistOrderResponse(10L, "다음지원자", "01012345678", CareerDetails.STUDENT, 2);
        given(adminApplyService.getWaitlistOrders(1L))
                .willReturn(WaitlistOrdersResponse.from(List.of(first, second)));

        // when, then
        mockMvc.perform(get("/admin/applies/waitlist-orders").param("recruitId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].applyId").value(30))
                .andExpect(jsonPath("$.data.content[1].applyId").value(10))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.data.hasNext").doesNotExist())
                .andExpect(jsonPath("$.data.size").doesNotExist());
    }

    @Test
    void 예비번호_목록조회에_공고가_누락되면_거부한다() throws Exception {
        // when, then
        mockMvc.perform(get("/admin/applies/waitlist-orders"))
                .andExpect(status().isBadRequest());
    }

    private ObjectNode editBody() throws Exception {
        return (ObjectNode) objectMapper.readTree("""
                {
                  "name": "김젝트", "phoneNumber": "01012345678", "email": "test@mail.com",
                  "careerDetails": "STUDENT", "region": "SEOUL", "experiencePeriod": "NONE",
                  "interestedDomains": ["GAME"], "selectionResult": "PASSED", "note": "비고",
                  "answers": {"1": "답변"}, "deletePortfolioIds": [10]
                }
                """);
    }

}
