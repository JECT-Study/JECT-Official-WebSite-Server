package org.ject.support.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.domain.member.dto.MemberDto.InitialProfileRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.UpdatePinRequest;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.ject.support.testconfig.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false", "server.port=0"})
class MemberControllerIntegrationTest extends ApplicationPeriodTest {

    private final String TEST_PIN = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";
    private final String TEST_NAME = "홍길동";
    private final String TEST_PHONE_NUMBER = "01012345678";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원 등록 API 통합 테스트")
    void registerMember_Integration() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN);

        // 실제 서비스를 사용하므로 모킹하지 않음
        // 대신 응답 상태만 확인

        // when & then
        mockMvc.perform(post("/members")
                        .cookie(new jakarta.servlet.http.Cookie("verificationToken", TEST_VERIFICATION_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // 응답 본문이 없으므로 상태 코드만 확인
    }

    @Test
    @DisplayName("임시회원 최초 프로필 등록 API 통합 테스트")
    @AuthenticatedUser(memberId = 1L)
    void registerInitialProfile_Integration() throws Exception {
        // given
        InitialProfileRequest request = new InitialProfileRequest(TEST_NAME, TEST_PHONE_NUMBER);

        // when & then
        mockMvc.perform(put("/members/profile/initial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("핀번호 재설정 API 통합 테스트")
    @AuthenticatedUser(memberId = 1L)
    void resetPin_Integration() throws Exception {
        // given
        UpdatePinRequest request = new UpdatePinRequest("654321"); // 새로운 PIN 번호

        // when & then
        mockMvc.perform(put("/members/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
