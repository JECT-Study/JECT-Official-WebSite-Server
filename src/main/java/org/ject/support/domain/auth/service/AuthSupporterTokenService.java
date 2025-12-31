package org.ject.support.domain.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.auth.exception.AuthException;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.external.discord.DiscordComponent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.ject.support.domain.auth.exception.AuthErrorCode.INVALID_CREDENTIALS;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthSupporterTokenService {

    private final long TOKEN_EXPIRATION_MILLIS = 10 * 60 * 1000; // 10분

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiscordComponent discordComponent;

    public void issueReadOnlyToken(String email, String pin) {
        Member member = memberRepository.findByEmailAndRole(email, Role.ADMIN)
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));

        if (!passwordEncoder.matches(pin, member.getPin())) {
            throw new AuthException(INVALID_CREDENTIALS);
        }

        Claims claims = Jwts.claims();
        claims.put("memberId", member.getId());
        claims.put("role", "ROLE_ADMIN");
        claims.put("tokenType", "TEMP");
        claims.put("purpose", "SUPPORTER_DATA_VIEW");
        claims.put("scopes", List.of("ADMIN_READ"));
        claims.setSubject("SYSTEM_SUPPORTER");

        String token = jwtTokenProvider.createToken(claims, TOKEN_EXPIRATION_MILLIS);

        discordComponent.sendSupporterTokenIssueMessage(email, token)
                .doOnError(e -> {
                    log.error("지원자 토큰 전송 실패: {}", email, e);
                })
                .subscribe();
    }
}
