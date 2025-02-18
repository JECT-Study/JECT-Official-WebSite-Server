package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.domain.recruit.dto.Constants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AccessPeriodVerificator {
    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(org.ject.support.common.util.PeriodAccessible)")
    public Object checkRecruitmentPeriod(ProceedingJoinPoint joinPoint) throws Throwable {
        Boolean isRecruiting = Boolean.valueOf(redisTemplate.opsForValue().get(Constants.PERIOD_FLAG));
        if (Boolean.FALSE.equals(isRecruiting)){
            throw new GlobalException(GlobalErrorCode.OVER_PERIOD);
        }
        return joinPoint.proceed();
    }
}
