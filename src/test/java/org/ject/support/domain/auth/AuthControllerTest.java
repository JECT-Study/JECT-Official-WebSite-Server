package org.ject.support.domain.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
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

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";

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

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";
    
    @Test
    @DisplayName("@PreAuthorize(\"permitAll()\") 설정으로 인증 없이 접근 가능한지 확인")
    void verifyAuthCode_WithPermitAll_ShouldAllowAccessWithoutAuthentication() throws Exception {
        // given
        VerifyAuthCodeRequest request = new VerifyAuthCodeRequest(TEST_EMAIL, TEST_AUTH_CODE);
        
        VerifyAuthCodeOnlyResponse response = new VerifyAuthCodeOnlyResponse(TEST_VERIFICATION_TOKEN);
        
        given(authService.verifyEmailByAuthCodeOnly(anyString(), anyString()))
            .willReturn(response);
        
        // when & then
        // 인증 없이 접근 가능한지 확인 (permitAll 설정)
        mockMvc.perform(post("/auth/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
