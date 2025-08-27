package org.ject.support.domain.admin.service;

import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.external.infrastructure.SlackRateLimiter;
import org.ject.support.external.slack.SlackComponent;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminAuthServiceTest {

    @InjectMocks
    AdminAuthService adminAuthService;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    MemberRepository memberRepository;

    @Mock
    SlackRateLimiter slackRateLimiter;

    @Mock
    SlackComponent slackComponent;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @Test
    void 입력받은_Email에_대한_관리자가_존재하지_않을_경우_NOT_FOUND_ADMIN_예외_발생() {
        // given
        String email = "test.com";
        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminAuthService.sendSlackAdminAuthCode(email))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);
    }

    @Test
    void 관리자_로그인_시도가_초당_허용량을_초과한_경우_TOO_MANY_REQUESTS_예외_발생() {
        // given
        String email = "test.com";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .role(Role.ADMIN)
                .build();
        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.of(adminMember));
        given(slackRateLimiter.tryConsume(1)).willReturn(false);

        // when, then
        assertThatThrownBy(() -> adminAuthService.sendSlackAdminAuthCode(email))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    void 관리자_로그인_시도를_성공한_경우_관리자의_Email_반환() {
        // given
        long memberId = 1L;
        String email = "test.com";
        Member adminMember = Member.builder()
                .id(memberId)
                .email(email)
                .role(Role.ADMIN)
                .build();
        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.of(adminMember));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(slackRateLimiter.tryConsume(1)).willReturn(true);

        // when
        String result = adminAuthService.sendSlackAdminAuthCode(email);

        // then
        verify(memberRepository).findByEmailAndRole(email, Role.ADMIN);
        verify(slackRateLimiter).tryConsume(1);
        assertEquals(email, result);
    }

    @Test
    void 슬랙_인증_코드_검증_시_입력받은_Email에_대한_관리자가_존재하지_않을_경우_NOT_FOUND_ADMIN_예외_발생() {
        // given
        String email = "test.com";
        String authCode = "ABC123";
        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminAuthService.verifySlackAdminAuthCode(email, authCode))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_ADMIN);
    }

    @Test
    void 슬랙_인증_코드_검증_시_인증_코드를_찾을_수_없는_경우_NOT_FOUND_AUTH_CODE_예외_발생() {
        // given
        String email = "test.com";
        String authCode = "ABC123";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.of(adminMember));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("admin-login:" + adminMember.getId())).willReturn(null);

        // when, then
        assertThatThrownBy(() -> adminAuthService.verifySlackAdminAuthCode(email, authCode))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.NOT_FOUND_AUTH_CODE);
    }

    @Test
    void 슬랙_인증_코드_검증_시_인증_코드가_일치하지_않는_경우_INVALID_AUTH_CODE_예외_발생() {
        // given
        String email = "test.com";
        String inputAuthCode = "ABC123";
        String storedCode = "XYZ789";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.of(adminMember));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("admin-login:" + adminMember.getId())).willReturn(storedCode);

        // when, then
        assertThatThrownBy(() -> adminAuthService.verifySlackAdminAuthCode(email, inputAuthCode))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.INVALID_AUTH_CODE);
    }

    @Test
    void 슬랙_인증_코드_검증에_성공한_경우_Authentication_객체_반환() {
        // given
        String email = "test.com";
        String inputAuthCode = "ABC123";
        String storedCode = "ABC123";
        String accessToken = "test.access.token";
        Member adminMember = Member.builder()
                .id(1L)
                .email(email)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByEmailAndRole(email, Role.ADMIN))
                .willReturn(Optional.of(adminMember));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("admin-login:" + adminMember.getId())).willReturn(storedCode);
        given(jwtTokenProvider.createAccessToken(authentication, adminMember.getId())).willReturn(accessToken);

        // when
        Authentication result = adminAuthService.verifySlackAdminAuthCode(email, inputAuthCode);

        // then
        verify(memberRepository).findByEmailAndRole(email, Role.ADMIN);
        verify(jwtTokenProvider).createAuthenticationByMember(adminMember);
    }
}
