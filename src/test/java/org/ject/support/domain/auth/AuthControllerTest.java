package org.ject.support.domain.auth;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ject.support.domain.auth.AuthDto.PinLoginRequest;
import org.ject.support.domain.auth.AuthDto.PinLoginResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeRequest;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";

    @Test
    @DisplayName("인증 코드 검증 및 인증 토큰 발급 성공")
    void verifyAuthCode_Success() {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(TEST_EMAIL, TEST_AUTH_CODE);
        VerifyAuthCodeOnlyResponse response = new VerifyAuthCodeOnlyResponse(TEST_VERIFICATION_TOKEN);
        
        given(authService.verifyEmailByAuthCodeOnly(request.email(), request.authCode()))
            .willReturn(response);

        // when
        VerifyAuthCodeOnlyResponse result = authController.verifyAuthCode(request);

        // then
        verify(authService).verifyEmailByAuthCodeOnly(TEST_EMAIL, TEST_AUTH_CODE);
        org.assertj.core.api.Assertions.assertThat(result.verificationToken()).isEqualTo(TEST_VERIFICATION_TOKEN);
    }
    
    @Test
    @DisplayName("PIN 로그인 성공")
    void loginWithPin_Success() {
        // given
        PinLoginRequest request = new PinLoginRequest(TEST_EMAIL, TEST_AUTH_CODE);
        PinLoginResponse response = new PinLoginResponse(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        
        given(authService.loginWithPin(request.email(), request.pin()))
            .willReturn(response);

        // when
        PinLoginResponse result = authController.loginWithPin(request);

        // then
        verify(authService).loginWithPin(TEST_EMAIL, TEST_AUTH_CODE);
        org.assertj.core.api.Assertions.assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        org.assertj.core.api.Assertions.assertThat(result.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("회원 존재 여부 확인 성공")
    void isExistMember_Success() {
        // given
        given(authService.isExistMember(TEST_EMAIL))
            .willReturn(true);

        // when
        boolean result = authController.isExistMember(TEST_EMAIL);

        // then
        verify(authService).isExistMember(TEST_EMAIL);
        org.assertj.core.api.Assertions.assertThat(result).isTrue();
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false", "server.port=0"})
class AuthControllerIntegrationTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    
    @Test
    @DisplayName("@PreAuthorize(\"permitAll()\") 설정으로 인증 없이 접근 가능한지 확인")
    void verifyAuthCode_WithPermitAll_ShouldAllowAccessWithoutAuthentication() throws Exception {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(TEST_EMAIL, TEST_AUTH_CODE);
        
        // when & then
        // 인증 없이 접근 가능한지 확인 (permitAll 설정)
        mockMvc.perform(post("/auth/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("PIN 로그인 API 인증 없이 접근 가능한지 확인")
    void loginWithPin_WithPermitAll_ShouldAllowAccessWithoutAuthentication() throws Exception {
        // given
        PinLoginRequest request = new PinLoginRequest(TEST_EMAIL, TEST_AUTH_CODE);
        
        // when & then
        // 인증 없이 접근 가능한지 확인 (permitAll 설정)
        mockMvc.perform(post("/auth/login/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("회원 존재 여부 확인 API 인증 없이 접근 가능한지 확인")
    void isExistMember_WithPermitAll_ShouldAllowAccessWithoutAuthentication() throws Exception {
        // when & then
        // 인증 없이 접근 가능한지 확인 (permitAll 설정)
        mockMvc.perform(get("/auth/login/exist?email=" + TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
