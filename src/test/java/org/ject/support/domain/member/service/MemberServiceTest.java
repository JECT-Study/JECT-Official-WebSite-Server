package org.ject.support.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.dto.MemberDto.InitialProfileRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterResponse;
import org.ject.support.domain.member.dto.MemberDto.UpdatePinRequest;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_PIN = "123456";
    private final String TEST_ENCODED_PIN = "encoded_pin";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";

    @BeforeEach
    void setUp() {
        // 기본 설정
    }

    @Test
    @DisplayName("임시 회원 등록 성공")
    void registerTempMember_Success() {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN);
        Member member = Member.builder()
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .build();

        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());
        given(passwordEncoder.encode(TEST_PIN)).willReturn(TEST_ENCODED_PIN);
        given(memberRepository.save(any(Member.class))).willReturn(member);
        given(jwtTokenProvider.createAuthenticationByMember(any(Member.class))).willReturn(authentication);
        given(jwtTokenProvider.createAccessToken(any(Authentication.class), any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.createRefreshToken(any(Authentication.class), any())).willReturn(TEST_REFRESH_TOKEN);

        // when
        RegisterResponse response = memberService.registerTempMember(request, TEST_EMAIL);

        // then
        assertThat(response.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
        verify(memberRepository).save(any(Member.class));
        verify(passwordEncoder).encode(TEST_PIN);
    }

    @Test
    @DisplayName("이미 존재하는 회원인 경우 예외 발생")
    void registerTempMember_AlreadyExistMember_ThrowsException() {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN);
        Member existingMember = Member.builder()
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .build();

        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.registerTempMember(request, TEST_EMAIL))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.ALREADY_EXIST_MEMBER);
    }
    
    @Test
    @DisplayName("회원 정보 업데이트 성공")
    void updateMember_Success() {
        // given
        Long memberId = 1L;
        InitialProfileRequest request = new InitialProfileRequest(TEST_NAME, TEST_PHONE_NUMBER);
        Member member = Member.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .build();
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        
        // when
        memberService.registerInitialProfile(request, memberId);
        
        // then
        assertThat(member.getName()).isEqualTo(TEST_NAME);
        assertThat(member.getPhoneNumber()).isEqualTo(TEST_PHONE_NUMBER);
        verify(memberRepository).findById(memberId);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 정보 업데이트 시 예외 발생")
    void updateMember_NotFoundMember_ThrowsException() {
        // given
        Long memberId = 1L;
        InitialProfileRequest request = new InitialProfileRequest(TEST_NAME, TEST_PHONE_NUMBER);
        
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> memberService.registerInitialProfile(request, memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
    }
    
    @Test
    @DisplayName("핀번호 재설정 성공")
    void updatePin_Success() {
        // given
        Long memberId = 1L;
        String newPin = "654321";
        UpdatePinRequest request = new UpdatePinRequest(newPin);
        
        Member member = Member.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .build();
        
        String newEncodedPin = "new_encoded_pin";
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(newPin, TEST_ENCODED_PIN)).willReturn(false); // 새 PIN이 기존과 다름
        given(passwordEncoder.encode(newPin)).willReturn(newEncodedPin);
        
        // when
        memberService.updatePin(request, memberId);
        
        // then
        assertThat(member.getPin()).isEqualTo(newEncodedPin);
        verify(memberRepository).findById(memberId);
        verify(passwordEncoder).matches(newPin, TEST_ENCODED_PIN);
        verify(passwordEncoder).encode(newPin);
    }
    
    @Test
    @DisplayName("핀번호 재설정 실패 - 존재하지 않는 회원")
    void updatePin_NotFoundMember_ThrowsException() {
        // given
        Long memberId = 1L;
        UpdatePinRequest request = new UpdatePinRequest("654321");
        
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> memberService.updatePin(request, memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
    }
    
    @Test
    @DisplayName("핀번호 재설정 실패 - 기존과 동일한 PIN 번호")
    void updatePin_SamePin_ThrowsException() {
        // given
        Long memberId = 1L;
        UpdatePinRequest request = new UpdatePinRequest(TEST_PIN);
        
        Member member = Member.builder()
                .id(memberId)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .build();
        
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(TEST_PIN, TEST_ENCODED_PIN)).willReturn(true); // 기존과 같은 PIN
        
        // when & then
        assertThatThrownBy(() -> memberService.updatePin(request, memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.SAME_PIN);
    }
}
