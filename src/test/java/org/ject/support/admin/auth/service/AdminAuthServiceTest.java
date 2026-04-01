package org.ject.support.admin.auth.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.admin.member.component.AdminMemberComponent;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
class AdminAuthServiceTest extends UnitTestSupport {

    @InjectMocks
    AdminAuthService adminAuthService;

    @Mock
    AdminMemberComponent adminMemberComponent;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    Authentication authentication;

    @Test
    void 관리자_로그인_성공_시_Authentication_객체_반환_및_실패_상태_초기화() {
        // given
        String email = "admin@ject.org";
        String password = "Password123";
        String failCountKey = "admin-login-password-fail-count:" + email;
        String blockKey = "admin-login-password-block:" + email;
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .pin("$2a$10$encodedPin")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(redisTemplate.hasKey(blockKey)).willReturn(false);
        given(adminMemberComponent.findMemberAdminByEmail(email)).willReturn(Optional.of(adminMember));
        given(passwordEncoder.matches(password, adminMember.getPin())).willReturn(true);
        given(jwtTokenProvider.createAuthenticationByMember(adminMember)).willReturn(authentication);

        // when
        adminAuthService.authenticateAdmin(email, password);

        // then
        verify(redisTemplate).delete(failCountKey);
        verify(redisTemplate).delete(blockKey);
        verify(jwtTokenProvider).createAuthenticationByMember(adminMember);
    }

    @Test
    void 운영팀_로그인_성공_시_Authentication_객체_반환() {
        // given
        String email = "operations@ject.org";
        String password = "Password123";
        String failCountKey = "admin-login-password-fail-count:" + email;
        String blockKey = "admin-login-password-block:" + email;
        Member operationsMember = Member.builder()
                .id(2L)
                .email(email)
                .pin("$2a$10$encodedPin")
                .status(MemberStatus.ACTIVE)
                .role(Role.OPERATIONS)
                .build();

        given(redisTemplate.hasKey(blockKey)).willReturn(false);
        given(adminMemberComponent.findMemberAdminByEmail(email)).willReturn(Optional.of(operationsMember));
        given(passwordEncoder.matches(password, operationsMember.getPin())).willReturn(true);
        given(jwtTokenProvider.createAuthenticationByMember(operationsMember)).willReturn(authentication);

        // when
        adminAuthService.authenticateAdmin(email, password);

        // then
        verify(redisTemplate).delete(failCountKey);
        verify(redisTemplate).delete(blockKey);
        verify(jwtTokenProvider).createAuthenticationByMember(operationsMember);
    }

    @Test
    void 관리자_로그인_시_이메일이_존재하지_않으면_INVALID_ADMIN_CREDENTIALS_예외_발생() {
        // given
        String email = "not-found@test.com";
        String password = "Password123!";
        String failCountKey = "admin-login-password-fail-count:" + email;
        given(adminMemberComponent.findMemberAdminByEmail(email)).willReturn(Optional.empty());
        given(redisTemplate.hasKey("admin-login-password-block:" + email)).willReturn(false);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(failCountKey)).willReturn(1L);

        // when, then
        assertThatThrownBy(() -> adminAuthService.authenticateAdmin(email, password))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_CREDENTIALS);
    }

    @Test
    void 관리자_로그인_시_비밀번호가_일치하지_않으면_INVALID_ADMIN_CREDENTIALS_예외_발생() {
        // given
        String email = "admin@ject.org";
        String password = "WrongPassword123";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .pin("$2a$10$encodedPin")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();
        String failCountKey = "admin-login-password-fail-count:" + email;

        given(adminMemberComponent.findMemberAdminByEmail(email)).willReturn(Optional.of(adminMember));
        given(redisTemplate.hasKey("admin-login-password-block:" + email)).willReturn(false);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(failCountKey)).willReturn(1L);
        given(passwordEncoder.matches(password, adminMember.getPin())).willReturn(false);

        // when, then
        assertThatThrownBy(() -> adminAuthService.authenticateAdmin(email, password))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_ADMIN_CREDENTIALS);
    }

    @Test
    void 관리자_로그인_시_LOCKED_상태면_LOCKED_ADMIN_예외_발생() {
        // given
        String email = "locked_admin@test.com";
        String password = "Password123!";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .pin("$2a$10$encodedPin")
                .status(MemberStatus.LOCKED)
                .role(Role.ADMIN)
                .build();

        given(adminMemberComponent.findMemberAdminByEmail(email)).willReturn(Optional.of(adminMember));
        given(redisTemplate.hasKey("admin-login-password-block:" + email)).willReturn(false);
        given(passwordEncoder.matches(password, adminMember.getPin())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> adminAuthService.authenticateAdmin(email, password))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.LOCKED_ADMIN);
    }

    @Test
    void 관리자_로그인_시도가_차단된_상태면_LOGIN_ATTEMPT_LIMITED_예외_발생() {
        // given
        String email = "admin@ject.org";
        String password = "Password123!";
        given(redisTemplate.hasKey("admin-login-password-block:" + email)).willReturn(true);

        // when, then
        assertThatThrownBy(() -> adminAuthService.authenticateAdmin(email, password))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.LOGIN_ATTEMPT_LIMITED);
    }

    @Test
    void 관리자_로그인_실패_횟수가_5회_이상이면_LOGIN_ATTEMPT_LIMITED_예외_발생() {
        // given
        String email = "not-found@test.com";
        String password = "Password123!";
        String failCountKey = "admin-login-password-fail-count:" + email;
        String blockKey = "admin-login-password-block:" + email;
        given(adminMemberComponent.findMemberAdminByEmail(email)).willReturn(Optional.empty());
        given(redisTemplate.hasKey(blockKey)).willReturn(false);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(failCountKey)).willReturn(5L);

        // when, then
        assertThatThrownBy(() -> adminAuthService.authenticateAdmin(email, password))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.LOGIN_ATTEMPT_LIMITED);
        verify(valueOperations).set(blockKey, "1", Duration.ofSeconds(60 * 60));
    }
}
