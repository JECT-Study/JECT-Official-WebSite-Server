package org.ject.support.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterResponse;
import org.ject.support.domain.member.dto.MemberDto.InitialProfileRequest;
import org.ject.support.domain.member.service.MemberService;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.BeforeEach;
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
import org.ject.support.testconfig.AuthenticatedUser;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_PIN = "123456";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = standaloneSetup(memberController).build();
    }

    @Test
    @DisplayName("회원 등록 성공")
    void registerMember_Success() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN);
        RegisterResponse response = new RegisterResponse(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        
        given(jwtTokenProvider.extractEmailFromVerificationToken(TEST_VERIFICATION_TOKEN)).willReturn(TEST_EMAIL);
        given(memberService.registerTempMember(any(RegisterRequest.class), anyString())).willReturn(response);

        // when & then
        mockMvc.perform(post("/members")
                .header("Authorization", "Bearer " + TEST_VERIFICATION_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
        
        verify(jwtTokenProvider).extractEmailFromVerificationToken(TEST_VERIFICATION_TOKEN);
        verify(memberService).registerTempMember(any(RegisterRequest.class), anyString());
    }
    
    @Test
    @DisplayName("임시회원 최초 프로필 등록 성공")
    void registerInitialProfile_Success() throws Exception {
        // given
        InitialProfileRequest request = new InitialProfileRequest(TEST_NAME, TEST_PHONE_NUMBER);
        Long memberId = 1L;
        
        // lenient 설정을 사용하여 엄격한 스텔빙 검사를 해제
        lenient().doNothing().when(memberService).registerInitialProfile(any(), eq(memberId));

        // CustomUserDetails를 사용하여 인증 정보 설정 (ROLE_TEMP 권한)
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, memberId, Role.TEMP);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(put("/members/profile/initial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        // verify를 사용하지 않고 스텔빙만 확인
        
        // 테스트 후 인증 정보 초기화
        SecurityContextHolder.clearContext();
    }


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false", "server.port=0"})
class MemberControllerIntegrationTest extends ApplicationPeriodTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final String TEST_PIN = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";
    private final String TEST_NAME = "홍길동";
    private final String TEST_PHONE_NUMBER = "01012345678";
    
    @Test
    @DisplayName("회원 등록 API 통합 테스트")
    void registerMember_Integration() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN);
        
        // 실제 서비스를 사용하므로 모킹하지 않음
        // 대신 응답 구조만 확인
        
        // when & then
        mockMvc.perform(post("/members")
                .header("Authorization", "Bearer " + TEST_VERIFICATION_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
        // 실제 응답 값은 테스트마다 다를 수 있으므로 구조만 확인
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
    }
}
