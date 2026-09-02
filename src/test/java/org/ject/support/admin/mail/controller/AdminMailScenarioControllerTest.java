package org.ject.support.admin.mail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.PreviewMailRequest;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioRequest.CustomVariableRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.service.MailPreviewService;
import org.ject.support.admin.mail.service.MailScenarioService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminMailScenarioControllerTest extends UnitTestSupport {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private MailScenarioService mailScenarioService;

    @Mock
    private MailPreviewService mailPreviewService;

    @InjectMocks
    private AdminMailScenarioController adminMailScenarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminMailScenarioController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .build();
    }

    @Test
    @DisplayName("메일 템플릿 목록을 기본 페이지 조건으로 조회한다")
    void 메일_템플릿_목록을_기본_페이지_조건으로_조회한다() throws Exception {
        // given
        MailScenarioResponse scenarioResponse = new MailScenarioResponse(
                1L, "테스트 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC,
                "TEST_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${name}", true, LocalDateTime.now(),
                List.of(
                        new MailScenarioResponse.CustomVariableResponse("RECRUIT_NAME", "모집명", "TEXT", true, null),
                        new MailScenarioResponse.CustomVariableResponse("INTERVIEW_AT", "면접 일시", "DATE_TIME", true, null)
                )
        );
        given(mailScenarioService.searchScenarios(
                isNull(MailScenarioCategory.class),
                isNull(MailScenarioType.class),
                any(Pageable.class)
        )).willReturn(new PageImpl<>(List.of(scenarioResponse), PageRequest.of(0, 10), 1));

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(scenarioResponse.id()))
                .andExpect(jsonPath("$.data.content[0].name").value(scenarioResponse.name()))
                .andExpect(jsonPath("$.data.content[0].category").value(scenarioResponse.category().name()))
                .andExpect(jsonPath("$.data.content[0].type").value(scenarioResponse.type().name()))
                .andExpect(jsonPath("$.data.content[0].customVariables[1].inputType").value("DATE_TIME"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(mailScenarioService).searchScenarios(
                isNull(MailScenarioCategory.class),
                isNull(MailScenarioType.class),
                pageableCaptor.capture()
        );
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort())
                .containsExactly(Sort.Order.desc("createdAt"));
    }

    @Test
    @DisplayName("메일 템플릿 목록을 구분과 타입으로 필터링한다")
    void 메일_템플릿_목록을_구분과_타입으로_필터링한다() throws Exception {
        // given
        MailScenarioResponse scenarioResponse = new MailScenarioResponse(
                1L, "테스트 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.REJECT,
                "TEST_SCENARIO", "제목", "본문", true, LocalDateTime.now(), List.of()
        );
        given(mailScenarioService.searchScenarios(
                eq(MailScenarioCategory.CLUB_MEMBER),
                eq(MailScenarioType.REJECT),
                any(Pageable.class)
        )).willReturn(new PageImpl<>(List.of(scenarioResponse), PageRequest.of(1, 10), 11));

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios")
                        .param("category", MailScenarioCategory.CLUB_MEMBER.name())
                        .param("type", MailScenarioType.REJECT.name())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].type").value(MailScenarioType.REJECT.name()))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.size").value(10));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(mailScenarioService).searchScenarios(
                eq(MailScenarioCategory.CLUB_MEMBER),
                eq(MailScenarioType.REJECT),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("시나리오를 성공적으로 생성한다")
    void createScenario() throws Exception {
        // given
        MailScenarioRequest request = new MailScenarioRequest(
                "새 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC, "NEW_SCENARIO",
                "[JECT] ${RECRUIT_NAME}", "${name}", true, 
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );
        MailScenarioResponse response = new MailScenarioResponse(
                1L, "새 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC,
                "NEW_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${name}", true, LocalDateTime.now(),
                List.of(new MailScenarioResponse.CustomVariableResponse("RECRUIT_NAME", "모집명", "TEXT", true, null))
        );
        given(mailScenarioService.createScenario(any(MailScenarioRequest.class))).willReturn(response);

        // w            hen & then
        mockMvc.perform(post("/admin/mails/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("새 시나리오"))
                .andExpect(jsonPath("$.data.category").value(MailScenarioCategory.CLUB_MEMBER.name()))
                .andExpect(jsonPath("$.data.type").value(MailScenarioType.ETC.name()));
    }

    @Test
    @DisplayName("시나리오를 성공적으로 수정한다")
    void updateScenario() throws Exception {
        // given
        Long scenarioId = 1L;
        MailScenarioRequest request = new MailScenarioRequest(
                "수정 시나리오", MailScenarioCategory.MAKERS, MailScenarioType.FINAL_PASS, "UPDATED_SCENARIO",
                "[JECT] ${RECRUIT_NAME}", "${name}", false, 
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.DATE_TIME, true, null))
        );
        MailScenarioResponse response = new MailScenarioResponse(
                scenarioId, "수정 시나리오", MailScenarioCategory.MAKERS, MailScenarioType.FINAL_PASS,
                "UPDATED_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${name}", false, LocalDateTime.now(),
                List.of(new MailScenarioResponse.CustomVariableResponse("RECRUIT_NAME", "모집명", "DATE_TIME", true, null))
        );
        given(mailScenarioService.updateScenario(eq(scenarioId), any(MailScenarioRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/admin/mails/scenarios/{scenarioId}", scenarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 시나리오"))
                .andExpect(jsonPath("$.data.category").value(MailScenarioCategory.MAKERS.name()))
                .andExpect(jsonPath("$.data.type").value(MailScenarioType.FINAL_PASS.name()))
                .andExpect(jsonPath("$.data.customVariables[0].inputType").value("DATE_TIME"));
    }

    @Test
    @DisplayName("시나리오를 성공적으로 삭제한다")
    void deleteScenario() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/mails/scenarios/{scenarioId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("시나리오 변수 목록을 성공적으로 반환한다")
    void getVariablesByScenario() throws Exception {
        // given
        Long scenarioId = 1L;
        MailScenarioVariableResponse response = new MailScenarioVariableResponse(
                scenarioId,
                "일반 구성원 - 예비 합격 통지",
                List.of(new MailScenarioVariableResponse.CustomVariableResponse("INTERVIEW_AT", "면접 일시", "DATE_TIME", true, null)),
                List.of("name", "semester")
        );

        given(mailScenarioService.getScenarioVariables(scenarioId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios/{scenarioId}/variables", scenarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.name").value("일반 구성원 - 예비 합격 통지"))
                .andExpect(jsonPath("$.data.customVariables[0].key").value("INTERVIEW_AT"))
                .andExpect(jsonPath("$.data.customVariables[0].inputType").value("DATE_TIME"))
                .andExpect(jsonPath("$.data.personalVariables[0]").value("name"));
    }

    @Test
    @DisplayName("지원자 기준으로 메일 미리보기 결과를 반환한다")
    void 지원자_기준으로_메일_미리보기_결과를_반환한다() throws Exception {
        // given
        PreviewMailRequest request = new PreviewMailRequest(1L, 20L, Map.of("MESSAGE", "안내 내용"));
        MailPreviewResponse response = new MailPreviewResponse(
                1L, 20L, "applicant@ject.kr", "홍길동님 안내", "안내 내용");
        given(mailPreviewService.preview(any(PreviewMailRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/mails/scenarios/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scenarioId").value(1))
                .andExpect(jsonPath("$.data.applyId").value(20))
                .andExpect(jsonPath("$.data.receiverEmail").value("applicant@ject.kr"))
                .andExpect(jsonPath("$.data.subject").value("홍길동님 안내"))
                .andExpect(jsonPath("$.data.body").value("안내 내용"));

        verify(mailPreviewService).preview(request);
    }

    @Test
    @DisplayName("날짜와 시간 입력 변수 타입을 생성 요청과 응답에서 유지한다")
    void 날짜와_시간_입력_변수_타입을_생성_요청과_응답에서_유지한다() throws Exception {
        MailScenarioRequest request = new MailScenarioRequest(
                "면접 안내", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC, "INTERVIEW_NOTICE",
                "면접 안내", "면접 일시: ${INTERVIEW_AT}", true,
                List.of(new CustomVariableRequest("INTERVIEW_AT", "면접 일시", VariableInputType.DATE_TIME, true, null))
        );
        MailScenarioResponse response = new MailScenarioResponse(
                1L, "면접 안내", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC,
                "INTERVIEW_NOTICE", "면접 안내", "면접 일시: ${INTERVIEW_AT}", true, LocalDateTime.now(),
                List.of(new MailScenarioResponse.CustomVariableResponse("INTERVIEW_AT", "면접 일시", "DATE_TIME", true, null))
        );
        given(mailScenarioService.createScenario(any(MailScenarioRequest.class))).willReturn(response);

        mockMvc.perform(post("/admin/mails/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customVariables[0].inputType").value("DATE_TIME"));
    }

    @Test
    @DisplayName("중복 시나리오 코드 생성 시 409 Conflict를 반환한다")
    void createScenario_duplicateCode() throws Exception {
        MailScenarioRequest request = new MailScenarioRequest(
                "새 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC, "DUP_CODE",
                "[JECT] ${RECRUIT_NAME}", "${name}", true, 
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );
        given(mailScenarioService.createScenario(any(MailScenarioRequest.class)))
                .willThrow(new MailException(MailErrorCode.DUPLICATE_SCENARIO_CODE)); // MAIL-5, CONFLICT

        mockMvc.perform(post("/admin/mails/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("MAIL-5"));
    }

    @Test
    @DisplayName("허용되지 않은 템플릿 변수 사용 시 400 Bad Request를 반환한다")
    void createScenario_unsupportedTemplateVariable() throws Exception {
        MailScenarioRequest request = new MailScenarioRequest(
                "새 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC, "NEW_SCENARIO",
                "[JECT] ${UNKNOWN}", "${name}", true, 
                List.of()
        );
        given(mailScenarioService.createScenario(any(MailScenarioRequest.class)))
                .willThrow(new MailException(MailErrorCode.UNSUPPORTED_TEMPLATE_VARIABLE)); // MAIL-3, BAD_REQUEST

        mockMvc.perform(post("/admin/mails/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("MAIL-3"));
    }

    @Test
    @DisplayName("잘못된 템플릿 문법 사용 시 400 Bad Request를 반환한다")
    void createScenario_invalidTemplateSyntax() throws Exception {
        MailScenarioRequest request = new MailScenarioRequest(
                "새 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC, "NEW_SCENARIO",
                "[JECT] ${UNCLOSED", "${name}", true, 
                List.of()
        );
        given(mailScenarioService.createScenario(any(MailScenarioRequest.class)))
                .willThrow(new MailException(MailErrorCode.INVALID_TEMPLATE_SYNTAX)); // MAIL-2, BAD_REQUEST

        mockMvc.perform(post("/admin/mails/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("MAIL-2"));
    }
}
