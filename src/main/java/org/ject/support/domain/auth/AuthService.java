package org.ject.support.domain.auth;

import static org.ject.support.common.security.jwt.JwtTokenProvider.createAccessCookie;
import static org.ject.support.common.security.jwt.JwtTokenProvider.createRefreshCookie;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.AuthCodeResponse;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.MemberDto.TempMemberJoinRequest;
import org.ject.support.domain.member.MemberRepository;
import org.ject.support.external.email.MailException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthCodeResponse verifyEmailByAuthCode(HttpServletResponse response, String email, String userInputCode) {
        verifyAuthCode(email, userInputCode);

        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            member = createTempMember(email);
        }

        Authentication authentication = jwtTokenProvider.createAuthenticationByMember(member);
        String accessToken = jwtTokenProvider.createAccessToken(authentication, member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        addCookie(response, accessToken, refreshToken);

        // TODO: 개발 시에만 json으로 토큰 반환. 추후 HTTPS 적용 및 실제 운영시에는 쿠키로만 전달 예정 (보안 이슈)
        return new AuthCodeResponse(accessToken, refreshToken);
    }

    private Member createTempMember(String email) {
        Member member = TempMemberJoinRequest.toEntity(email);
        return memberRepository.save(member);
    }


    // 쿠키 추가 메서드
    private void addCookie(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(createAccessCookie(accessToken));
        response.addCookie(createRefreshCookie(refreshToken));
    }

    public void verifyAuthCode(String key, String userInputCode) {
        // redis에서 키 값으로 인증 번호 조회
        String redisCode = redisTemplate.opsForValue().get(key);

        // Redis에서 코드가 없는 경우
        if (redisCode == null) {
            throw new MailException(NOT_FOUND_AUTH_CODE);
        }

        // 코드 불일치
        if (!userInputCode.equals(redisCode)) {
            throw new MailException(INVALID_AUTH_CODE);
        }

        // 인증 성공
        redisTemplate.delete(key);
    }
}
