package org.ject.support.admin.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.admin.member.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.entity.Member;
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

    private final RedisTemplate<String, String> redisTemplate;
    private final AdminMemberComponent adminMemberComponent;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_LOGIN_PASSWORD_FAIL_COUNT_KEY_PREFIX = "admin-login-password-fail-count:";
    private static final String ADMIN_LOGIN_PASSWORD_BLOCK_KEY_PREFIX = "admin-login-password-block:";
    private static final int ADMIN_LOGIN_PASSWORD_MAX_FAILURE_COUNT = 5;
    private static final long ADMIN_LOGIN_PASSWORD_BLOCK_TIME = 60 * 60;

    @Transactional
    public Authentication authenticateAdmin(String email, String password) {
        validateLoginAttemptLimit(email);

        Member member = adminMemberComponent.findBackofficeMemberByEmail(email)
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
}
