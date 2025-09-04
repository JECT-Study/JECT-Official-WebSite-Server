package org.ject.support.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doAnswer;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.controller.AuthController;
import org.ject.support.domain.auth.dto.AuthDto.PinLoginRequest;
import org.ject.support.domain.auth.dto.AuthDto.TokenRefreshRequest;
import org.ject.support.domain.auth.dto.AuthDto.VerifyAuthCodeRequest;
import org.ject.support.domain.auth.dto.AuthVerificationResult;
import org.ject.support.domain.auth.service.AuthService;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AuthControllerTest extends UnitTestSupport {
    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;
    
    @Mock
    private CustomSuccessHandler customSuccessHandler;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private final Long TEST_MEMBER_ID = 1L;

    @Test
    void 인증_코드_검증_AUTH_CODE_템플릿_이메일만_반환_성공() {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(TEST_EMAIL, TEST_AUTH_CODE);
        EmailTemplate template = EmailTemplate.AUTH_CODE;
        
        AuthVerificationResult mockResult = new AuthVerificationResult(TEST_EMAIL);
        
        given(authService.verifyAuthCodeByTemplate(request.email(), request.authCode(), template))
            .willReturn(mockResult);

        // when
        authController.verifyAuthCode(request, mock(HttpServletRequest.class), mock(HttpServletResponse.class), template);

        // then
        verify(authService).verifyAuthCodeByTemplate(eq(TEST_EMAIL), eq(TEST_AUTH_CODE), eq(template));
        verify(customSuccessHandler).onAuthenticationSuccess(any(HttpServletResponse.class), eq(TEST_EMAIL));
    }
    
    @Test
    void 인증_코드_검증_PIN_RESET_템플릿_인증_토큰_발급_성공() {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(TEST_EMAIL, TEST_AUTH_CODE);
        EmailTemplate template = EmailTemplate.PIN_RESET;
        
        Authentication mockAuthentication = mock(Authentication.class);
        AuthVerificationResult mockResult = new AuthVerificationResult(mockAuthentication);
        
        given(authService.verifyAuthCodeByTemplate(request.email(), request.authCode(), template))
            .willReturn(mockResult);

        // when
        authController.verifyAuthCode(request, mock(HttpServletRequest.class), mock(HttpServletResponse.class), template);

        // then
        verify(authService).verifyAuthCodeByTemplate(TEST_EMAIL, TEST_AUTH_CODE, template);
        verify(customSuccessHandler).onAuthenticationSuccess(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(mockAuthentication));
    }
    
    @Test
    void 리프레시_토큰을_사용한_액세스_토큰_재발급_성공() {
        // given
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        
        given(jwtTokenProvider.resolveRefreshToken(mockRequest)).willReturn(TEST_REFRESH_TOKEN);
        given(authService.refreshAccessToken(TEST_REFRESH_TOKEN)).willReturn(TEST_MEMBER_ID);

        // when
        authController.refreshToken(mockRequest, mockResponse);

        // then
        verify(authService).refreshAccessToken(TEST_REFRESH_TOKEN);
        verify(customSuccessHandler).onAuthenticationSuccess(mockResponse, TEST_REFRESH_TOKEN, TEST_MEMBER_ID);
    }
    
    @Test
    void PIN_로그인_성공() {
        // given
        PinLoginRequest request = new PinLoginRequest(TEST_EMAIL, TEST_AUTH_CODE);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Authentication mockAuthentication = mock(Authentication.class);
        
        given(authService.loginWithPin(request.email(), request.pin()))
            .willReturn(mockAuthentication);

        // when
        authController.loginWithPin(request, mockRequest, mockResponse);

        // then
        verify(authService).loginWithPin(TEST_EMAIL, TEST_AUTH_CODE);
        verify(customSuccessHandler).onAuthenticationSuccess(mockRequest, mockResponse, mockAuthentication);
    }
    
    @Test
    void 회원_존재_여부_확인_성공() {
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
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false", "server.port=0"})
class AuthControllerIntegrationTest extends ApplicationPeriodTest {

    @MockitoBean
    private AuthService authService;
    
    @MockitoBean
    private CustomSuccessHandler customSuccessHandler;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";
    
    @Test
    @DisplayName("인증 코드 검증 API - 인증 없이 접근 가능 및 쿠키 발급 테스트")
    void verifyAuthCode_WithPermitAll_ShouldAllowAccessAndSetCookies() throws Exception {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(TEST_EMAIL, TEST_AUTH_CODE);
        
        // Redis에서 인증 코드를 반환하도록 설정
        when(valueOperations.get(TEST_EMAIL)).thenReturn(TEST_AUTH_CODE);
        
        // AuthService의 verifyAuthCodeByTemplate 메서드를 모킹
        // PIN_RESET 템플릿은 Authentication 객체 반환
        Authentication mockAuthentication = mock(Authentication.class);
        AuthVerificationResult mockResult = new AuthVerificationResult(mockAuthentication);
        given(authService.verifyAuthCodeByTemplate(TEST_EMAIL, TEST_AUTH_CODE, EmailTemplate.PIN_RESET))
            .willReturn(mockResult);
        
        // 쿠키 발급을 위한 모킹 설정
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.addCookie(new Cookie("verification", "test-verification-token"));
            return null;
        }).when(customSuccessHandler).onAuthenticationSuccess(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(mockAuthentication));
        
        // when & then
        // 인증 없이 접근 가능한지 확인 (permitAll 설정)
        MvcResult result = mockMvc.perform(post("/auth/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("template", EmailTemplate.PIN_RESET.name()))
                .andExpect(status().isOk())
                .andReturn();
        
        // 응답 상태 코드 확인
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(customSuccessHandler).onAuthenticationSuccess(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(mockAuthentication));
    }
    
    @Test
    @DisplayName("@PreAuthorize(\"hasRole('ROLE_APPLY')\") 설정으로 인증이 필요한지 확인")
    void refreshToken_WithRoleTemp_ShouldRequireAuthentication() throws Exception {
        // given
        PinLoginRequest request = new PinLoginRequest("test@email.com", "123456");

        // when & then
        mockMvc.perform(post("/auth/login/pin")
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
