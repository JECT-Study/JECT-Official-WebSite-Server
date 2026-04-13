package org.ject.support.admin.mail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioRequest.CustomVariableRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.service.MailScenarioService;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminMailScenarioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private MailScenarioService mailScenarioService;

    @InjectMocks
    private AdminMailScenarioController adminMailScenarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminMailScenarioController)
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .build();
    }

    @Test
    @DisplayName("시나리오 목록을 성공적으로 조회한다")
    void getScenarios() throws Exception {
        // given
        MailScenarioResponse scenarioResponse = new MailScenarioResponse(
                1L, "테스트 시나리오", MailScenarioCategory.CLUB_MEMBER, MailScenarioType.ETC,
                "TEST_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${name}", true, LocalDateTime.now(),
                List.of(new MailScenarioResponse.CustomVariableResponse("RECRUIT_NAME", "모집명", "TEXT", true, null))
        );
        given(mailScenarioService.getScenarios()).willReturn(List.of(scenarioResponse));

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(scenarioResponse.id()))
                .andExpect(jsonPath("$.data[0].name").value(scenarioResponse.name()))
                .andExpect(jsonPath("$.data[0].category").value(scenarioResponse.category().name()))
                .andExpect(jsonPath("$.data[0].type").value(scenarioResponse.type().name()));
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
                List.of(new CustomVariableRequest("RECRUIT_NAME", "모집명", VariableInputType.TEXT, true, null))
        );
        MailScenarioResponse response = new MailScenarioResponse(
                scenarioId, "수정 시나리오", MailScenarioCategory.MAKERS, MailScenarioType.FINAL_PASS,
                "UPDATED_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${name}", false, LocalDateTime.now(),
                List.of(new MailScenarioResponse.CustomVariableResponse("RECRUIT_NAME", "모집명", "TEXT", true, null))
        );
        given(mailScenarioService.updateScenario(eq(scenarioId), any(MailScenarioRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/admin/mails/scenarios/{scenarioId}", scenarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 시나리오"))
                .andExpect(jsonPath("$.data.category").value(MailScenarioCategory.MAKERS.name()))
                .andExpect(jsonPath("$.data.type").value(MailScenarioType.FINAL_PASS.name()));
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
                List.of(new MailScenarioVariableResponse.CustomVariableResponse("RECRUIT_ALERT_APPLY_URL", "모집 알림 신청 URL", "URL", true, null)),
                List.of("name", "semester")
        );

        given(mailScenarioService.getScenarioVariables(scenarioId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios/{scenarioId}/variables", scenarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.name").value("일반 구성원 - 예비 합격 통지"))
                .andExpect(jsonPath("$.data.customVariables[0].key").value("RECRUIT_ALERT_APPLY_URL"))
                .andExpect(jsonPath("$.data.customVariables[0].inputType").value("URL"))
                .andExpect(jsonPath("$.data.personalVariables[0]").value("name"));
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
