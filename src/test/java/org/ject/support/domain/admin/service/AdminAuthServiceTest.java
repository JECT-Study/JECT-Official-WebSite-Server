package org.ject.support.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.admin.exception.AdminErrorCode;
import org.ject.support.domain.admin.exception.AdminException;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.external.infrastructure.SlackRateLimiter;
import org.ject.support.external.slack.SlackComponent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class AdminAuthServiceTest extends UnitTestSupport {

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
}