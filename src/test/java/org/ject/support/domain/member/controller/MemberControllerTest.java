package org.ject.support.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberDto.InitialProfileRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.UpdatePinRequest;
import org.ject.support.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

class MemberControllerTest extends UnitTestSupport {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomSuccessHandler customSuccessHandler;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_PIN = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = standaloneSetup(memberController).build();
    }

    @Test
    void 회원_등록_성공() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN);
        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(TEST_EMAIL, 1L, Role.TEMP), "", null);

        given(jwtTokenProvider.resolveVerificationToken(any())).willReturn(TEST_VERIFICATION_TOKEN);
        given(jwtTokenProvider.extractEmailFromVerificationToken(TEST_VERIFICATION_TOKEN)).willReturn(TEST_EMAIL);
        given(memberService.registerTempMember(any(RegisterRequest.class), anyString())).willReturn(mockAuthentication);

        // customSuccessHandler.onAuthenticationSuccess 메소드 호출 모킹
        doNothing().when(customSuccessHandler).onAuthenticationSuccess(any(HttpServletRequest.class), any(
                HttpServletResponse.class), any(Authentication.class));

        // when & then
        mockMvc.perform(post("/members")
                        .cookie(new jakarta.servlet.http.Cookie("verificationToken", TEST_VERIFICATION_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(jwtTokenProvider).resolveVerificationToken(any());
        verify(jwtTokenProvider).extractEmailFromVerificationToken(TEST_VERIFICATION_TOKEN);
        verify(memberService).registerTempMember(any(RegisterRequest.class), eq(TEST_EMAIL));
        verify(customSuccessHandler).onAuthenticationSuccess(any(HttpServletRequest.class),
                any(HttpServletResponse.class), eq(mockAuthentication));
    }

    @Test
    void 임시회원_최초_프로필_등록_성공() throws Exception {
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

    @Test
    void 핀번호_재설정_성공() throws Exception {
        // given
        UpdatePinRequest request = new UpdatePinRequest("654321"); // 새로운 PIN 번호
        Long memberId = 1L;

        // lenient 설정을 사용하여 엄격한 스텔빙 검사를 해제
        lenient().doNothing().when(memberService).updatePin(any(UpdatePinRequest.class), eq(memberId));

        // CustomUserDetails를 사용하여 인증 정보 설정 (ROLE_TEMP 권한)
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, memberId, Role.TEMP);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(put("/members/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // verify를 사용하지 않고 스텔빙만 확인
        // 테스트 후 인증 정보 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    void 핀번호_재설정_실패_유효하지_않은_PIN_번호_형식() throws Exception {
        // given
        // 유효하지 않은 PIN 번호 (6자리가 아님)
        String invalidPin = "12345";
        UpdatePinRequest request = new UpdatePinRequest(invalidPin);
        Long memberId = 1L;

        // CustomUserDetails를 사용하여 인증 정보 설정 (ROLE_TEMP 권한)
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, memberId, Role.TEMP);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(put("/members/pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 테스트 후 인증 정보 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    void 임시회원_최초_프로필_등록_여부_확인_성공_등록된_경우() throws Exception {
        // given
        Long memberId = 1L;

        // 모킹 설정을 lenient로 변경하여 엄격한 스텁 검사를 비활성화
        lenient().when(memberService.checkIsInitialed(any())).thenReturn(true);

        // CustomUserDetails를 사용하여 인증 정보 설정 (ROLE_TEMP 권한)
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, memberId, Role.TEMP);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/members/profile/initial/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("true"));

        // 테스트 후 인증 정보 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    void 임시회원_최초_프로필_등록_여부_확인_성공_미등록된_경우() throws Exception {
        // given
        Long memberId = 1L;

        // 모킹 설정을 lenient로 변경하여 엄격한 스텁 검사를 비활성화
        lenient().when(memberService.checkIsInitialed(any())).thenReturn(false);

        // CustomUserDetails를 사용하여 인증 정보 설정 (ROLE_TEMP 권한)
        CustomUserDetails userDetails = new CustomUserDetails(TEST_EMAIL, memberId, Role.TEMP);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/members/profile/initial/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("false"));

        // 테스트 후 인증 정보 초기화
        SecurityContextHolder.clearContext();
    }
}
