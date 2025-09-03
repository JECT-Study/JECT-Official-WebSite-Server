package org.ject.support.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.common.util.CodeGeneratorUtil;
import org.ject.support.domain.admin.component.AdminMemberComponent;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.external.infrastructure.SlackRateLimiter;
import org.ject.support.external.slack.SlackComponent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AdminMemberComponent adminMemberComponent;
    private final SlackRateLimiter slackRateLimiter;
    private final SlackComponent slackComponent;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX = "admin-login:";
    private static final String ADMIN_LOGIN_AUTH_CODE_FAIL_COUNT_KEY_PREFIX = "admin-login-fail-count:";
    private static final int ADMIN_LOGIN_AUTH_CODE_LENGTH = 6;
    private static final long ADMIN_LOGIN_AUTH_CODE_EXPIRATION = 3 * 60;
    private static final long ADMIN_LOGIN_AUTH_CODE_FAIL_LOCK_TIME = 10 * 60;
    private static final int ADMIN_LOGIN_MAX_FAILURE_COUNT = 3;

    public String sendSlackAdminAuthCode(String email) {
        Member member = adminMemberComponent.getMemberAdminByEmail(email);
        checkMemberStatus(member);

        String authCode = CodeGeneratorUtil.generateUpperAlphaNumCode(ADMIN_LOGIN_AUTH_CODE_LENGTH);
        String key = ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX + member.getId();

        if (slackRateLimiter.tryConsume(1)) {
            redisTemplate.opsForValue().set(key, authCode, Duration.ofSeconds(ADMIN_LOGIN_AUTH_CODE_EXPIRATION));
            slackComponent.sendAdminLoginMessage(makeAdminLoginMessage(member.getEmail(), authCode));
        } else {
            throw new AdminException(AdminErrorCode.TOO_MANY_REQUESTS);
        }

        return member.getEmail();
    }

    @Transactional
    public Authentication verifySlackAdminAuthCode(String email, String authCode) {
        Member member = adminMemberComponent.getMemberAdminByEmail(email);

        checkMemberStatus(member);
        verifyAuthCode(authCode, member);

        return jwtTokenProvider.createAuthenticationByMember(member);
    }

    private void checkMemberStatus(Member member) {
        if (member.getStatus() == MemberStatus.LOCKED) {
            throw new AdminException(AdminErrorCode.LOCKED_ADMIN);
        }
    }

    private void verifyAuthCode(String userInputCode, Member member) {
        String authCodeKey = ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX + member.getId();
        String storedCode = redisTemplate.opsForValue().get(authCodeKey);

        if (storedCode == null) {
            throw new AdminException(AdminErrorCode.NOT_FOUND_AUTH_CODE);
        }

        if (storedCode.equals(userInputCode)) {
            redisTemplate.delete(authCodeKey);
            return;
        }

        handleInvalidAuthCode(member);
    }

    private void handleInvalidAuthCode(Member member) {
        String failCountKey = ADMIN_LOGIN_AUTH_CODE_FAIL_COUNT_KEY_PREFIX + member.getId();
        int failCount = getFailCount(failCountKey) + 1;

        if (failCount >= ADMIN_LOGIN_MAX_FAILURE_COUNT) {
            adminMemberComponent.changeMemberStatus(member, MemberStatus.LOCKED);
        }

        redisTemplate.opsForValue().set(
                failCountKey,
                String.valueOf(failCount),
                Duration.ofSeconds(ADMIN_LOGIN_AUTH_CODE_FAIL_LOCK_TIME)
        );

        throw new AdminException(AdminErrorCode.INVALID_AUTH_CODE);
    }

    private int getFailCount(String failCountKey) {
        String failCountStr = redisTemplate.opsForValue().get(failCountKey);
        return failCountStr == null ? 0 : Integer.parseInt(failCountStr);
    }

    private String makeAdminLoginMessage(String email, String code) {
        return "관리자 로그인 : { " + email + " } 인증 코드를 입력해 주세요 [" + code + "]";
    }
}
