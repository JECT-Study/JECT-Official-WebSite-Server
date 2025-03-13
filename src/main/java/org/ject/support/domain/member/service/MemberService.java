package org.ject.support.domain.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.ALREADY_EXIST_MEMBER;
import static org.ject.support.domain.member.exception.MemberErrorCode.SAME_PIN;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.dto.MemberDto;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.RegisterResponse;
import org.ject.support.domain.member.dto.MemberDto.UpdatePinRequest;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 인증된 사용자의 PIN 번호를 설정하고 임시 회원을 생성합니다.
     * 인증번호 검증 -> PIN 번호 설정 -> 임시 회원 생성 단계에서 호출됩니다.
     * 인증 토큰을 통해 검증된 이메일로 회원을 조회하고, PIN 번호를 암호화하여 저장합니다.
     */
    @Transactional
    public RegisterResponse registerTempMember(RegisterRequest registerRequest, String email) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElse(null);

        if (member == null) {
            // 새로운 회원 생성
            member = createTempMemberWithPin(registerRequest, email);
        } else {
            // 기존 회원이 있는 경우 PIN 번호만 업데이트
            throw new MemberException(ALREADY_EXIST_MEMBER);
        }

        // 인증 및 토큰 발급
        Authentication authentication = jwtTokenProvider.createAuthenticationByMember(member);
        String accessToken = jwtTokenProvider.createAccessToken(authentication, member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new RegisterResponse(accessToken, refreshToken);
    }

    /**
     * PIN 번호가 설정된 임시 회원을 생성합니다.
     * PIN 번호는 암호화하여 저장합니다.
     */
    private Member createTempMemberWithPin(RegisterRequest registerRequest, String email) {
        String encodedPin = passwordEncoder.encode(registerRequest.pin());
        Member member = registerRequest.toEntity(email, encodedPin);
        return memberRepository.save(member);
    }

    /**
     * 임시회원의 최초 프로필 정보(이름, 전화번호) 등록
     * 임시회원(ROLE_TEMP)이 최초로 이름과 전화번호를 등록할 때 호출됩니다.
     */
    @Transactional
    public void registerInitialProfile(MemberDto.InitialProfileRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        member.updateNameAndPhoneNumber(request.name(), request.phoneNumber());
    }

    @Transactional
    public void updatePin(UpdatePinRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (passwordEncoder.matches(request.pin(), member.getPin())) {
            throw new MemberException(SAME_PIN);
        }

        String encodedPin = passwordEncoder.encode(request.pin());
        member.updatePin(encodedPin);
    }
}
