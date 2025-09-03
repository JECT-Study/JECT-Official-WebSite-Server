package org.ject.support.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.dto.MemberDto.InitialProfileRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.UpdatePinRequest;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest extends UnitTestSupport {

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

    @Mock
    private OngoingSemesterProvider ongoingSemesterProvider;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_PIN = "123456";
    private final String TEST_ENCODED_PIN = "encoded_pin";

    @BeforeEach
    void setUp() {
        // 기본 설정
    }

    @Test
    void 임시_회원_등록_성공() {
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
        given(ongoingSemesterProvider.getOngoingSemesterId()).willReturn(1L);
        // when
        Authentication result = memberService.registerTempMember(request, TEST_EMAIL);

        // then
        assertThat(result).isEqualTo(authentication);
        verify(memberRepository).save(any(Member.class));
        verify(passwordEncoder).encode(TEST_PIN);
        verify(jwtTokenProvider).createAuthenticationByMember(any(Member.class));
    }

    @Test
    void 이미_존재하는_회원인_경우_예외_발생() {
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
    void 회원_정보_업데이트_성공() {
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
    void 존재하지_않는_회원_정보_업데이트_시_예외_발생() {
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
    void 핀번호_재설정_성공() {
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
        given(passwordEncoder.encode(newPin)).willReturn(newEncodedPin);
        
        // when
        memberService.updatePin(request, memberId);
        
        // then
        assertThat(member.getPin()).isEqualTo(newEncodedPin);
        verify(memberRepository).findById(memberId);
        verify(passwordEncoder).encode(newPin);
    }
    
    @Test
    void 핀번호_재설정_실패_존재하지_않는_회원() {
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
}
