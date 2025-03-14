package org.ject.support.domain.auth;


import static org.ject.support.domain.auth.AuthErrorCode.EXPIRED_REFRESH_TOKEN;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_CREDENTIALS;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;
import static org.ject.support.domain.member.exception.MemberErrorCode.NOT_FOUND_MEMBER;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.TokenRefreshResponse;
import org.ject.support.domain.auth.AuthDto.PinLoginResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * 인증번호 검증만 수행하고 임시 토큰을 발급합니다.
     * 이메일 인증 -> 인증번호 입력 단계에서 호출됩니다.
     * 인증번호 검증이 성공하면 임시 토큰을 발급하여 반환합니다.
     * 이 토큰은 사용자 정보를 포함하지 않고, 인증번호 검증이 완료되었음을 증명하는 용도로만 사용됩니다.
     * @param email 인증할 이메일 주소
     * @param userInputCode 사용자가 입력한 인증번호
     */
    @Transactional
    public VerifyAuthCodeOnlyResponse verifyEmailByAuthCodeOnly(String email, String userInputCode) {
        // 인증번호 검증
        verifyAuthCode(email, userInputCode);

        // 임시 토큰 발급 - 인증번호 검증이 완료되었음을 증명하는 토큰
        String verificationToken = jwtTokenProvider.createVerificationToken(email);

        // 임시 토큰 반환
        return new VerifyAuthCodeOnlyResponse(verificationToken);
    }
    
    /**
     * PIN 번호를 사용하여 로그인합니다.
     * 이메일과 PIN 번호를 검증하고, 성공 시 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 
     * @param email 사용자 이메일
     * @param pin 사용자 PIN 번호
     */
    @Transactional
    public PinLoginResponse loginWithPin(String email, String pin) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        
        // PIN 번호 검증
        if (!passwordEncoder.matches(pin, member.getPin())) {
            throw new AuthException(INVALID_CREDENTIALS);
        }
        
        // 인증 및 토큰 발급
        Authentication authentication = jwtTokenProvider.createAuthenticationByMember(member);
        String accessToken = jwtTokenProvider.createAccessToken(authentication, member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, member.getId());
        
        return new PinLoginResponse(accessToken, refreshToken);
    }

    private void verifyAuthCode(String email, String userInputCode) {
        // redis에서 키 값으로 인증 번호 조회
        String redisCode = redisTemplate.opsForValue().get(email);

        // Redis에서 코드가 없는 경우
        if (redisCode == null) {
            throw new AuthException(NOT_FOUND_AUTH_CODE);
        }

        // 코드 불일치
        if (!userInputCode.equals(redisCode)) {
            throw new AuthException(INVALID_AUTH_CODE);
        }

        // 인증 성공
        redisTemplate.delete(email);
    }

    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급된 액세스 토큰이 포함된 응답 객체
     * @throws AuthException 리프레시 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        try {
            // 리프레시 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new AuthException(INVALID_REFRESH_TOKEN);
            }

            Long memberId = jwtTokenProvider.extractMemberId(refreshToken);
            // 새 액세스 토큰 발급
            String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken, memberId);

            return new TokenRefreshResponse(newAccessToken);
        } catch (ExpiredJwtException e) {
            throw new AuthException(EXPIRED_REFRESH_TOKEN);
        } catch (JwtException e) {
            throw new AuthException(INVALID_REFRESH_TOKEN);
        }
    }

    public boolean isExistMember(String email) {
        return memberRepository.existsByEmail(email);
    }
}
