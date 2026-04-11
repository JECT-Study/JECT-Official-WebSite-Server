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
import java.util.List;
import java.util.Set;
import org.ject.support.admin.mail.domain.MailVariable;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.service.MailScenarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminMailScenarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "security.cors.allowed-origins=http://localhost:8080",
        "security.cors.allowed-origins-client=http://localhost:3000",
        "security.cors.allowed-origins-client-dev=http://localhost:3000"
})
class AdminMailScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MailScenarioService mailScenarioService;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private org.ject.support.common.security.jwt.JwtExceptionHandlerFilter jwtExceptionHandlerFilter;

    @Test
    @DisplayName("시나리오 목록을 성공적으로 조회한다")
    void getScenarios() throws Exception {
        // given
        MailScenarioResponse scenarioResponse = new MailScenarioResponse(
                1L, "테스트 시나리오", "테스트 카테고리",
                "TEST_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${NAME}", true,
                List.of(new MailScenarioResponse.VariableResponse("VAR1", "라벨1")),
                List.of(new MailScenarioResponse.VariableResponse("VAR2", "라벨2"))
        );
        given(mailScenarioService.getScenarios()).willReturn(List.of(scenarioResponse));

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(scenarioResponse.id()))
                .andExpect(jsonPath("$.data[0].name").value(scenarioResponse.name()))
                .andExpect(jsonPath("$.data[0].category").value(scenarioResponse.category()));
    }

    @Test
    @DisplayName("시나리오를 성공적으로 생성한다")
    void createScenario() throws Exception {
        // given
        MailScenarioRequest request = new MailScenarioRequest(
                "새 시나리오", "테스트 카테고리", "NEW_SCENARIO",
                "[JECT] ${RECRUIT_NAME}", "${NAME}", true, Set.of(MailVariable.NAME, MailVariable.RECRUIT_NAME)
        );
        MailScenarioResponse response = new MailScenarioResponse(
                1L, "새 시나리오", "테스트 카테고리",
                "NEW_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${NAME}", true,
                List.of(new MailScenarioResponse.VariableResponse("RECRUIT_NAME", "모집명")),
                List.of(new MailScenarioResponse.VariableResponse("NAME", "이름"))
        );
        given(mailScenarioService.createScenario(any(MailScenarioRequest.class))).willReturn(response);

        // w            hen & then
        mockMvc.perform(post("/admin/mails/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("새 시나리오"))
                .andExpect(jsonPath("$.data.category").value("테스트 카테고리"));
    }

    @Test
    @DisplayName("시나리오를 성공적으로 수정한다")
    void updateScenario() throws Exception {
        // given
        Long scenarioId = 1L;
        MailScenarioRequest request = new MailScenarioRequest(
                "수정 시나리오", "테스트 카테고리", "UPDATED_SCENARIO",
                "[JECT] ${RECRUIT_NAME}", "${NAME}", false, Set.of(MailVariable.NAME, MailVariable.RECRUIT_NAME)
        );
        MailScenarioResponse response = new MailScenarioResponse(
                scenarioId, "수정 시나리오", "테스트 카테고리",
                "UPDATED_SCENARIO", "[JECT] ${RECRUIT_NAME}", "${NAME}", false,
                List.of(new MailScenarioResponse.VariableResponse("RECRUIT_NAME", "모집명")),
                List.of(new MailScenarioResponse.VariableResponse("NAME", "이름"))
        );
        given(mailScenarioService.updateScenario(eq(scenarioId), any(MailScenarioRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/admin/mails/scenarios/{scenarioId}", scenarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 시나리오"))
                .andExpect(jsonPath("$.data.category").value("테스트 카테고리"));
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
                List.of(new MailScenarioVariableResponse.VariableResponse("RECRUIT_ALERT_APPLY_URL", "모집 알림 신청 URL")),
                List.of(new MailScenarioVariableResponse.VariableResponse("NAME", "이름"))
        );

        given(mailScenarioService.getScenarioVariables(scenarioId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/mails/scenarios/{scenarioId}/variables", scenarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scenarioId").value(scenarioId))
                .andExpect(jsonPath("$.data.name").value("일반 구성원 - 예비 합격 통지"))
                .andExpect(jsonPath("$.data.commonVariables[0].key").value("RECRUIT_ALERT_APPLY_URL"))
                .andExpect(jsonPath("$.data.personalVariables[0].label").value("이름"));
    }
}
