package org.ject.support.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.CodeGeneratorUtil;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.external.slack.SlackComponent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final SlackComponent slackComponent;

    private static final String ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX = "admin-login:";
    private static final int ADMIN_LOGIN_AUTH_CODE_LENGTH = 6;
    private static final long ADMIN_LOGIN_AUTH_CODE_EXPIRATION = 3 * 60; // 3분

    public String sendSlackAdminAuthCode(String email) {
        Member member = memberRepository.findByEmailAndRole(email, Role.ADMIN)
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));

        String authCode = CodeGeneratorUtil.generateAlphaNumCode(ADMIN_LOGIN_AUTH_CODE_LENGTH);
        String key = ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX + member.getId();
        redisTemplate.opsForValue().set(key, authCode, Duration.ofSeconds(ADMIN_LOGIN_AUTH_CODE_EXPIRATION));

        slackComponent.sendAdminLoginMessage(makeAdminLoginMessage(member.getName(), authCode));

        return member.getEmail();
    }

    private String makeAdminLoginMessage(String name, String code) {
        return "관리자 로그인 : [" + name + "] 인증 코드를 입력해 주세요 {" + code + "}";
    }
}
