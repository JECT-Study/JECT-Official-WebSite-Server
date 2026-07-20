package org.ject.support.domain.recruit.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ject.support.common.data.redis.resilience.RedisCacheCircuitBreakerProvider;
import org.ject.support.common.data.redis.resilience.RedisCacheExceptionClassifier;
import org.ject.support.common.data.redis.resilience.RedisCacheExecutionContext;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.Constants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AccessPeriodVerifier {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    @Around("@annotation(org.ject.support.common.util.PeriodAccessible) && @annotation(target)")
    public Object checkRecruitmentPeriod(ProceedingJoinPoint joinPoint, PeriodAccessible target) throws Throwable {
        // 이미 상위(Aspect 등)에서 레디스 장애를 감지하여 우회 모드라면 안전하게 통과 (가용성 우선)
        if (RedisCacheExecutionContext.isBypassEnabled()) {
            return joinPoint.proceed();
        }

        CircuitBreaker circuitBreaker = circuitBreakerProvider.get("access-period");

        // 서킷 브레이커 상태 체크 (OPEN 상태면 즉시 통과하여 장애 전파 방지)
        if (!circuitBreaker.tryAcquirePermission()) {
            log.warn("AccessPeriodVerifier circuit is open. Falling back to allow access.");
            return joinPoint.proceed();
        }

        try {
            if (target.recruitIdParameterIndex() >= 0) {
                Long recruitId = extractRecruitId(joinPoint, target.recruitIdParameterIndex());
                validateRecruiting(isRecruiting(getKeyName(recruitId)));
                return proceed(joinPoint, circuitBreaker);
            }

            // 모든 직군에 대한 요청인 경우, 하나의 어떤 RECRUIT_FLAG만 있어도 허용
            if (target.permitAllJob()) {
                boolean isAnyRecruiting = Arrays.stream(JobFamily.values())
                        .anyMatch(jobFamily -> isRecruiting(getKeyName(jobFamily.name())));
                validateRecruiting(isAnyRecruiting);
            } else {
                // 특정 직군을 파라미터로 받은 경우, 해당 직군에 대한 모집 여부로 판단
                JobFamily jobFamily = extractJobFamily(joinPoint);
                validateRecruiting(isRecruiting(getKeyName(jobFamily.name())));
            }

            return proceed(joinPoint, circuitBreaker);
        } catch (Throwable throwable) {
            // 레디스 관련 인프라 장애(연결, 타임아웃 등) 시 폴백 처리
            if (RedisCacheExceptionClassifier.isRedisRelated(throwable)) {
                log.warn("AccessPeriodVerifier failed due to Redis issue. Falling back to allow access. error={}", throwable.getMessage());
                // 실패 기록 및 컨텍스트에 장애 발생 마킹
                circuitBreaker.onError(0, TimeUnit.NANOSECONDS, throwable);
                RedisCacheExecutionContext.markFailure("access-period");
                return joinPoint.proceed();
            }

            // 비즈니스 로직 예외(모집 기간 종료 등)나 그 외 버그성 예외는 그대로 전파
            throw throwable;
        }
    }

    private Object proceed(ProceedingJoinPoint joinPoint, CircuitBreaker circuitBreaker) throws Throwable {
        Object result = joinPoint.proceed();
        // 성공 기록
        circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS);
        return result;
    }

    private String getKeyName(String jobFamilyName) {
        return String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, jobFamilyName);
    }

    private String getKeyName(Long recruitId) {
        return String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, recruitId);
    }

    private boolean isRecruiting(String keyName) {
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(keyName));
    }

    private void validateRecruiting(boolean isRecruiting) {
        if (!isRecruiting) {
            throw new GlobalException(GlobalErrorCode.OVER_PERIOD);
        }
    }

    private JobFamily extractJobFamily(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(JobFamily.class::isInstance)
                .map(JobFamily.class::cast)
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.MISS_REQUIRED_JOB_FAMILY_PARAMETER));
    }

    private Long extractRecruitId(ProceedingJoinPoint joinPoint, int recruitIdParameterIndex) {
        Object[] args = joinPoint.getArgs();
        if (recruitIdParameterIndex >= args.length || !(args[recruitIdParameterIndex] instanceof Long recruitId)) {
            throw new GlobalException(GlobalErrorCode.MISS_REQUIRED_REQUEST_PARAMETER);
        }
        return recruitId;
    }
}
