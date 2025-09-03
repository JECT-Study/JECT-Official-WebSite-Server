package org.ject.support.domain.recruit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class AccessPeriodVerifierTest extends UnitTestSupport {

    @InjectMocks
    AccessPeriodVerifier accessPeriodVerifier;

    @Mock
    ProceedingJoinPoint joinPoint;

    @Mock
    PeriodAccessible target;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 하나의_직군이라도_모집중인_상태에서_permitAllJob이_true면_통과() throws Throwable {
        // given
        when(target.permitAllJob()).thenReturn(true); // 모든 직군에 대한 요청
        when(valueOperations.get(anyString())).thenReturn("false");
        when(valueOperations.get("RECRUIT_FLAG:FE")).thenReturn("true"); // 하나라도 모집중
        when(joinPoint.proceed()).thenReturn("OK");

        // when
        Object result = accessPeriodVerifier.checkRecruitmentPeriod(joinPoint, target);

        // then
        assertThat(result).isEqualTo("OK");
    }

    @Test
    void 모집중인_직군이_없는_상태에서_permitAllJob이_true이면_실패() {
        // given
        when(target.permitAllJob()).thenReturn(true); // 모든 직군에 대한 요청
        when(valueOperations.get(anyString())).thenReturn("false");

        // when, then
        assertThatThrownBy(() -> accessPeriodVerifier.checkRecruitmentPeriod(joinPoint, target))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void permitAllJob이_false인_상태에서_모집중인_직군을_파라미터로_전달하면_통과() throws Throwable {
        // given
        when(target.permitAllJob()).thenReturn(false);
        when(joinPoint.getArgs()).thenReturn(new Object[]{JobFamily.BE});
        when(valueOperations.get(anyString())).thenReturn("false");
        when(valueOperations.get("RECRUIT_FLAG:BE")).thenReturn("true");
        when(joinPoint.proceed()).thenReturn("OK");

        // when
        Object result = accessPeriodVerifier.checkRecruitmentPeriod(joinPoint, target);

        // then
        assertThat(result).isEqualTo("OK");
    }

    @Test
    void permitAllJob이_false인_상태에서_모집중이지_않은_직군을_파라미터로_전달하면_실패() {
        // given
        when(target.permitAllJob()).thenReturn(false);
        when(joinPoint.getArgs()).thenReturn(new Object[]{JobFamily.BE});
        when(valueOperations.get(anyString())).thenReturn("false");

        // when, then
        assertThatThrownBy(() -> accessPeriodVerifier.checkRecruitmentPeriod(joinPoint, target))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void permitAll이_false인데_JobFamily_파라미터_없으면_실패() {
        // given
        when(target.permitAllJob()).thenReturn(false);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"string", 123}); // JobFamily 없음

        // when, then
        assertThatThrownBy(() -> accessPeriodVerifier.checkRecruitmentPeriod(joinPoint, target))
                .isInstanceOf(GlobalException.class);
    }
}