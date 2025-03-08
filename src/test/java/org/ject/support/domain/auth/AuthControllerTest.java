package org.ject.support.domain.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ject.support.common.security.jwt.JwtCookieProvider;
import org.ject.support.domain.auth.AuthDto.AuthCodeResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeRequest;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    // 기존 단위 테스트 유지

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private JwtCookieProvider jwtCookieProvider;

    @Mock
    private HttpServletResponse response;

    private final String TEST_NAME = "test";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";

    @Test
    @DisplayName("인증 코드 검증 및 쿠키 설정 성공")
    void verifyAuthCode_Success() {
        // given
        VerifyAuthCodeRequest request =
                new VerifyAuthCodeRequest(TEST_NAME, TEST_EMAIL, TEST_PHONE_NUMBER, TEST_AUTH_CODE);
        
        AuthCodeResponse authResponse = new AuthCodeResponse(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        
        given(authService.verifyEmailByAuthCode(
            request.name(),
            request.email(),
            request.phoneNumber(),
            request.authCode()
        )).willReturn(authResponse);

        // when
        authController.verifyAuthCode(response, request);

        // then
        verify(jwtCookieProvider).createAccessCookie(TEST_ACCESS_TOKEN);
        verify(jwtCookieProvider).createRefreshCookie(TEST_REFRESH_TOKEN);
        verify(response, times(2)).addCookie(any()); // access token과 refresh token 쿠키 각각 설정
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false", "server.port=0"})
@ExtendWith(MockitoExtension.class)
class AuthControllerIntegrationTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    private final String TEST_NAME = "test";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";
    
    @Test
    @DisplayName("@PreAuthorize(\"permitAll()\") 설정으로 인증 없이 접근 가능한지 확인")
    void verifyAuthCode_WithPermitAll_ShouldAllowAccessWithoutAuthentication() throws Exception {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(
                TEST_NAME, TEST_EMAIL, TEST_PHONE_NUMBER, TEST_AUTH_CODE);
        
        AuthCodeResponse authResponse = new AuthCodeResponse(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        
        given(authService.verifyEmailByAuthCode(
                anyString(), anyString(), anyString(), anyString())
        ).willReturn(authResponse);
        
        // when & then
        // 인증 없이 접근 가능한지 확인 (permitAll 설정)
        mockMvc.perform(post("/auth/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
