package org.ject.support.admin.auth.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.admin.member.component.AdminMemberComponent;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.external.infrastructure.DiscordRateLimiter;
import org.ject.support.external.notification.event.AdminLoginNotificationEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    private ValueOperations<String, String> valueOperations;

    @Mock
    MemberRepository memberRepository;

    @Mock
    DiscordRateLimiter discordRateLimiter;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Test
    void 관리자로그인_1차인증시_LOCKED_상태의_상태의_계정일_경우_LOCKED_ADMIN_예외_발생() {
        // given
        String email = "locked_admin@test.com";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.LOCKED)
                .role(Role.ADMIN)
                .build();
        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn(adminMember);

        // when, then
        assertThatThrownBy(() -> adminAuthService.sendAdminAuthCode(email))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.LOCKED_ADMIN);
    }

    @Test
    void 관리자_로그인_시도가_초당_허용량을_초과한_경우_TOO_MANY_REQUESTS_예외_발생() {
        // given
        String email = "test.com";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();
        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn(adminMember);
        given(discordRateLimiter.tryConsume(1)).willReturn(false);

        // when, then
        assertThatThrownBy(() -> adminAuthService.sendAdminAuthCode(email))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    void 관리자_로그인_인증코드_요청_시_이벤트를_발행한다() {
        // given
        long memberId = 1L;
        String email = "test.com";
        Member adminMember = Member.builder()
                .id(memberId)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn(adminMember);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(discordRateLimiter.tryConsume(1)).willReturn(true);

        // when
        String result = adminAuthService.sendAdminAuthCode(email);

        // then
        verify(adminMemberComponent).getMemberAdminByEmail(email);
        verify(discordRateLimiter).tryConsume(1);
        verify(applicationEventPublisher)
                .publishEvent(any(AdminLoginNotificationEvent.class));
        assertEquals(email, result);
    }

    @Test
    void 관리자로그인_2차인증시_LOCKED_상태의__관리자_계정일_경우_LOCKED_ADMIN_예외_발생() {
        // given
        String email = "locked_admin@test.com";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.LOCKED)
                .role(Role.ADMIN)
                .build();
        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn((adminMember));

        // when, then
        assertThatThrownBy(() -> adminAuthService.verifyAdminAuthCode(email, "ABC123"))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.LOCKED_ADMIN);
    }

    @Test
    void 인증_코드_검증_시_인증_코드를_찾을_수_없는_경우_NOT_FOUND_AUTH_CODE_예외_발생() {
        // given
        String email = "test.com";
        String authCode = "ABC123";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn(adminMember);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("admin-login:" + adminMember.getId())).willReturn(null);

        // when, then
        assertThatThrownBy(() -> adminAuthService.verifyAdminAuthCode(email, authCode))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_AUTH_CODE);
    }

    @Test
    void 인증_코드_검증_시_인증_코드가_일치하지_않는_경우_INVALID_AUTH_CODE_예외_발생() {
        // given
        String email = "test.com";
        String inputAuthCode = "ABC123";
        String storedCode = "XYZ789";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();
        String authCodeKey = "admin-login:" + adminMember.getId();
        String failCountKey = "admin-login-fail-count:" + adminMember.getId();
        String failCountStr = "1";

        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn(adminMember);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(authCodeKey)).willReturn(storedCode);
        given(valueOperations.get(failCountKey)).willReturn(failCountStr);

        // when, then
        assertThatThrownBy(() -> adminAuthService.verifyAdminAuthCode(email, inputAuthCode))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_AUTH_CODE);
    }

    @Test
    void 인증_코드_검증에_성공한_경우_Authentication_객체_반환() {
        // given
        String email = "test.com";
        String inputAuthCode = "ABC123";
        String storedCode = "ABC123";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();
        String authCodeKey = "admin-login:" + adminMember.getId();

        given(adminMemberComponent.getMemberAdminByEmail(email)).willReturn(adminMember);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(authCodeKey)).willReturn(storedCode);
        given(redisTemplate.delete(authCodeKey)).willReturn(true);

        // when
        adminAuthService.verifyAdminAuthCode(email, inputAuthCode);

        // then
        verify(adminMemberComponent).getMemberAdminByEmail(email);
        verify(jwtTokenProvider).createAuthenticationByMember(adminMember);
        verify(redisTemplate).delete(authCodeKey);
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
