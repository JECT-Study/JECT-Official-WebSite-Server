package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 스프링 캐시 인터셉터보다 먼저 실행되어 캐시 접근을 우회할지 결정
@RequiredArgsConstructor
public class CacheCircuitBreakerAspect {

    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    private final CacheOperationSource cacheOperationSource = new AnnotationCacheOperationSource();

    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable) || " +
            "@annotation(org.springframework.cache.annotation.CachePut) || " +
            "@annotation(org.springframework.cache.annotation.CacheEvict) || " +
            "@annotation(org.springframework.cache.annotation.Caching) || " +
            "@within(org.springframework.cache.annotation.Cacheable) || " +
            "@within(org.springframework.cache.annotation.CachePut) || " +
            "@within(org.springframework.cache.annotation.CacheEvict) || " +
            "@within(org.springframework.cache.annotation.Caching)")
    public void cacheOperation() {}

    @Around("cacheOperation()")
    public Object aroundCacheCall(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);

        // 메서드 및 클래스 레벨에서 참조하는 모든 캐시 이름을 추출
        Collection<CacheOperation> operations = cacheOperationSource.getCacheOperations(method, targetClass);
        List<String> cacheNames = extractCacheNames(operations);
        
        // 읽기 작업인 경우 swallow exception, 쓰기 작업인 경우 실패 시 throw exception
        boolean strictWrite = isStrictWriteRequired(operations);


        // 캐시별 서킷 브레이커로부터 실행 권한을 획득 시도
        List<CircuitBreakerExecution> grantedBreakers = new ArrayList<>(cacheNames.size());
        boolean bypassEnabled = false;
        for (String cacheName : cacheNames) {
            CircuitBreaker circuitBreaker = circuitBreakerProvider.get(cacheName);
            // 레디스 호출 방지를 위해 OPEN 상태는 즉시 권한을 거부
            if (!circuitBreaker.tryAcquirePermission()) {
                bypassEnabled = true;
                log.debug("Cache circuit is open. cache={}, method={}", cacheName, method.getName());
                continue;
            }

            // 메서드 실행 후 성공 여부를 기록하기 위해 획득한 서킷 브레이커를 보관
            grantedBreakers.add(new CircuitBreakerExecution(cacheName, circuitBreaker));
        }

        // ThreadLocal 우회 설정을 스택에 쌓아 중첩된 호출에서도 상태를 유지
        RedisCacheExecutionContext.pushContext(bypassEnabled, strictWrite);

        try {
            // 메서드 실행을 계속, 우회가 활성화된 경우 캐시 레이어는 패스
            Object result = joinPoint.proceed();
            // 실제 레디스 캐시를 사용한 경우(우회되지 않은 경우)에만 성공을 기록
            if (!bypassEnabled) {
                recordSuccess(grantedBreakers);
            }
            return result;
        } finally {
            // 이번 호출의 컨텍스트를 제거하여 이전 호출 상태로 복구
            RedisCacheExecutionContext.popContext();
            // 최상위 호출이 종료될 때 전체 상태를 정리하여 메모리 누수를 방지
            if (!RedisCacheExecutionContext.hasContext()) {
                RedisCacheExecutionContext.clear();
            }
        }
    }

    // 이번 호출에서 레디스 장애가 발생하지 않은 캐시들에 대해서만 성공을 기록
    private void recordSuccess(final List<CircuitBreakerExecution> grantedBreakers) {
        for (CircuitBreakerExecution execution : grantedBreakers) {
            // 캐시 조회/저장 중 장애가 발생했다면, ErrorHandler에서 이미 실패로 처리
            if (RedisCacheExecutionContext.hasFailure(execution.cacheName())) {
                continue;
            }
            // 단순 성공 여부만 필요하므로 지연 시간(duration)은 0으로 기록
            execution.circuitBreaker().onSuccess(0, TimeUnit.NANOSECONDS);
        }
    }

    // CacheOperationSource를 활용하여 메서드 및 클래스 레벨에 선언된 모든 캐시 이름을 추출
    private List<String> extractCacheNames(final Collection<CacheOperation> operations) {
        if (operations == null || operations.isEmpty()) {
            return List.of();
        }

        return operations.stream()
                .flatMap(operation -> operation.getCacheNames().stream())
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .toList();
    }

    // 모든 작업이 @Cacheable인 경우(단순 조회 목적)가 아니라면 엄격한 예외 처리가 필요하다고 판단
    private boolean isStrictWriteRequired(final Collection<CacheOperation> operations) {
        if (operations == null || operations.isEmpty()) {
            return true;
        }

        // 하나라도 @CachePut이나 @CacheEvict가 섞여 있다면 정합성을 위해 strict 모드 활성화
        return operations.stream().anyMatch(op -> !(op instanceof CacheableOperation));
    }

    // 각 캐시별 서킷 브레이커 실행 상태를 담는 불변 객체
    private record CircuitBreakerExecution(String cacheName, CircuitBreaker circuitBreaker) {}
}
