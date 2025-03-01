package org.ject.support.domain.auth;


import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtCookieProvider;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.AuthCodeResponse;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.MemberDto.TempMemberJoinRequest;
import org.ject.support.domain.member.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthCodeResponse verifyEmailByAuthCode(String name, String email,
                                                  String phoneNumber, String userInputCode) {
        verifyAuthCode(email, userInputCode);

        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            member = createTempMember(name, email, phoneNumber);
        }

        Authentication authentication = jwtTokenProvider.createAuthenticationByMember(member);
        String accessToken = jwtTokenProvider.createAccessToken(authentication, member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new AuthCodeResponse(accessToken, refreshToken);
    }

    private Member createTempMember(String name,  String email, String phoneNumber) {
        Member member = TempMemberJoinRequest.toEntity(name, email, phoneNumber);
        return memberRepository.save(member);
    }

    public void verifyAuthCode(String email, String userInputCode) {
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
}
