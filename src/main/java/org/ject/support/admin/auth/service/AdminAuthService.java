package org.ject.support.admin.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.common.util.CodeGeneratorUtil;
import org.ject.support.admin.member.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.external.infrastructure.DiscordRateLimiter;
import org.ject.support.external.notification.event.AdminLoginNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final AdminMemberComponent adminMemberComponent;
    private final DiscordRateLimiter discordRateLimiter;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX = "admin-login:";
    private static final String ADMIN_LOGIN_AUTH_CODE_FAIL_COUNT_KEY_PREFIX = "admin-login-fail-count:";
    private static final String ADMIN_LOGIN_PASSWORD_FAIL_COUNT_KEY_PREFIX = "admin-login-password-fail-count:";
    private static final String ADMIN_LOGIN_PASSWORD_BLOCK_KEY_PREFIX = "admin-login-password-block:";
    private static final int ADMIN_LOGIN_AUTH_CODE_LENGTH = 6;
    private static final long ADMIN_LOGIN_AUTH_CODE_EXPIRATION = 3 * 60;
    private static final long ADMIN_LOGIN_AUTH_CODE_FAIL_LOCK_TIME = 10 * 60;
    private static final int ADMIN_LOGIN_MAX_FAILURE_COUNT = 3;
    private static final int ADMIN_LOGIN_PASSWORD_MAX_FAILURE_COUNT = 5;
    private static final long ADMIN_LOGIN_PASSWORD_BLOCK_TIME = 60 * 60;

    public String sendAdminAuthCode(String email) {
        Member member = adminMemberComponent.getMemberAdminByEmail(email);
        checkMemberStatus(member);

        String authCode = CodeGeneratorUtil.generateUpperAlphaNumCode(ADMIN_LOGIN_AUTH_CODE_LENGTH);
        String key = ADMIN_LOGIN_AUTH_CODE_KEY_PREFIX + member.getId();

        if (discordRateLimiter.tryConsume(1)) {
            redisTemplate.opsForValue().set(key, authCode, Duration.ofSeconds(ADMIN_LOGIN_AUTH_CODE_EXPIRATION));

            applicationEventPublisher
                    .publishEvent(new AdminLoginNotificationEvent(email, authCode));
        } else {
            throw new AdminException(AdminErrorCode.TOO_MANY_REQUESTS);
        }

        return member.getEmail();
    }

    @Transactional
    public Authentication verifyAdminAuthCode(String email, String authCode) {
        Member member = adminMemberComponent.getMemberAdminByEmail(email);

        checkMemberStatus(member);
        verifyAuthCode(authCode, member);

        return jwtTokenProvider.createAuthenticationByMember(member);
    }

    @Transactional
    public Authentication authenticateAdmin(String email, String password) {
        validateLoginAttemptLimit(email);

        Member member = adminMemberComponent.findMemberAdminByEmail(email)
                .orElseThrow(() -> handleInvalidAdminCredentials(email));

        if (!passwordEncoder.matches(password, member.getPin())) {
            throw handleInvalidAdminCredentials(email);
        }

        checkMemberStatus(member);
        clearLoginFailState(email);

        return jwtTokenProvider.createAuthenticationByMember(member);
    }

    private AdminException handleInvalidAdminCredentials(String email) {
        String failCountKey = ADMIN_LOGIN_PASSWORD_FAIL_COUNT_KEY_PREFIX + email;
        Long failCount = redisTemplate.opsForValue().increment(failCountKey);

        if (Long.valueOf(1L).equals(failCount)) {
            redisTemplate.expire(failCountKey, Duration.ofSeconds(ADMIN_LOGIN_PASSWORD_BLOCK_TIME));
        }

        if (failCount != null && failCount >= ADMIN_LOGIN_PASSWORD_MAX_FAILURE_COUNT) {
            String blockKey = ADMIN_LOGIN_PASSWORD_BLOCK_KEY_PREFIX + email;
            redisTemplate.opsForValue().set(blockKey, "1", Duration.ofSeconds(ADMIN_LOGIN_PASSWORD_BLOCK_TIME));
            throw new AdminException(AdminErrorCode.LOGIN_ATTEMPT_LIMITED);
        }

        return new AdminException(AdminErrorCode.INVALID_ADMIN_CREDENTIALS);
    }

    private void validateLoginAttemptLimit(String email) {
        String blockKey = ADMIN_LOGIN_PASSWORD_BLOCK_KEY_PREFIX + email;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new AdminException(AdminErrorCode.LOGIN_ATTEMPT_LIMITED);
        }
    }

    private void clearLoginFailState(String email) {
        redisTemplate.delete(ADMIN_LOGIN_PASSWORD_FAIL_COUNT_KEY_PREFIX + email);
        redisTemplate.delete(ADMIN_LOGIN_PASSWORD_BLOCK_KEY_PREFIX + email);
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
}
