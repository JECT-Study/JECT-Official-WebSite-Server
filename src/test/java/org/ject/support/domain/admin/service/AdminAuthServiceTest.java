package org.ject.support.domain.admin.service;

import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.external.infrastructure.SlackRateLimiter;
import org.ject.support.external.slack.SlackComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("입력받은 Email에 대한 관리자가 존재하지 않을 경우, NOT_FOUND_ADMIN 예외 발생")
    void sendSlackAdminAuthCode_NotFoundAdmin() {
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
    @DisplayName("관리자 로그인 시도가 초당 허용량을 초과한 경우, TOO_MANY_REQUESTS 예외 발생")
    void sendSlackAdminAuthCode_TooManyRequests() {
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
    @DisplayName("관리자 로그인 시도를 성공한 경우, 관리자의 Email 반환")
    void sendSlackAdminAuthCode_Success() {
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
}