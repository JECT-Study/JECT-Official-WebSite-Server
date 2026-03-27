package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 스프링 캐시 인터셉터보다 먼저 실행되어 캐시 접근을 우회할지 결정
@RequiredArgsConstructor
public class CacheCircuitBreakerAspect {

    // 캐시 이름별로 서킷 브레이커 인스턴스를 관리하고 반환하는 프로바이더
    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    /**
     * @Cacheable, @CachePut, @CacheEvict, @Caching 어노테이션이 사용된 메서드나 클래스를 가로챕니다.
     */
    @Around("@annotation(org.springframework.cache.annotation.Cacheable) || " +
            "@annotation(org.springframework.cache.annotation.CachePut) || " +
            "@annotation(org.springframework.cache.annotation.CacheEvict) || " +
            "@annotation(org.springframework.cache.annotation.Caching) || " +
            "@within(org.springframework.cache.annotation.Cacheable) || " +
            "@within(org.springframework.cache.annotation.CachePut) || " +
            "@within(org.springframework.cache.annotation.CacheEvict) || " +
            "@within(org.springframework.cache.annotation.Caching)")
    public Object aroundCacheCall(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 실제 대상 객체의 메서드를 찾아 인터페이스나 상속 관계에서의 어노테이션까지 감지할 수 있게 합니다.
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), joinPoint.getTarget().getClass());

        // 메서드 및 클래스 레벨에서 참조하는 모든 캐시 이름을 추출
        List<String> cacheNames = extractCacheNames(method);
        // 이번 요청에서 실행 권한을 획득한 서킷 브레이커들을 추적
        List<CircuitBreakerExecution> grantedBreakers = new ArrayList<>(cacheNames.size());
        // 참조하는 캐시 중 하나라도 OPEN 상태면 이번 호출은 캐시를 우회
        boolean bypassEnabled = false;

        // 캐시별 서킷 브레이커로부터 실행 권한을 획득 시도
        for (String cacheName : cacheNames) {
            CircuitBreaker circuitBreaker = circuitBreakerProvider.get(cacheName);
            // 레디스 호출 방지를 위해 OPEN 상태는 즉시 권한을 거부(Fail-fast)
            if (!circuitBreaker.tryAcquirePermission()) {
                bypassEnabled = true;
                log.debug("Cache circuit is open. cache={}, method={}", cacheName, method.getName());
                continue;
            }

            // 메서드 실행 후 성공 여부를 기록하기 위해 획득한 서킷 브레이커를 보관합니다.
            grantedBreakers.add(new CircuitBreakerExecution(cacheName, circuitBreaker));
        }

        // ThreadLocal 우회 설정을 스택에 쌓아 중첩된 호출에서도 상태를 유지
        RedisCacheExecutionContext.pushContext(bypassEnabled);

        try {
            // 메서드 실행을 계속,  우회가 활성화된 경우 캐시 레이어는 패스
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

    // @Caching을 포함하여 메서드 및 클래스 레벨에 선언된 모든 캐시 관련 어노테이션에서 캐시 이름을 추출
    private List<String> extractCacheNames(final Method method) {
        Set<String> names = new HashSet<>();
        Class<?> targetClass = method.getDeclaringClass();

        // 클래스 레벨의 @CacheConfig를 통해 기본 캐시 이름을 확보합니다.
        CacheConfig cacheConfig = AnnotatedElementUtils.findMergedAnnotation(targetClass, CacheConfig.class);
        String[] defaultNames = (cacheConfig != null) ? cacheConfig.cacheNames() : new String[0];

        // @Cacheable 추출
        Cacheable mCacheable = AnnotatedElementUtils.findMergedAnnotation(method, Cacheable.class);
        Cacheable cCacheable = (mCacheable == null) ? AnnotatedElementUtils.findMergedAnnotation(targetClass, Cacheable.class) : null;
        if (mCacheable != null) names.addAll(resolveNames(mCacheable.cacheNames(), mCacheable.value(), defaultNames));
        if (cCacheable != null) names.addAll(resolveNames(cCacheable.cacheNames(), cCacheable.value(), defaultNames));

        // @CachePut 추출
        CachePut mCachePut = AnnotatedElementUtils.findMergedAnnotation(method, CachePut.class);
        CachePut cCachePut = (mCachePut == null) ? AnnotatedElementUtils.findMergedAnnotation(targetClass, CachePut.class) : null;
        if (mCachePut != null) names.addAll(resolveNames(mCachePut.cacheNames(), mCachePut.value(), defaultNames));
        if (cCachePut != null) names.addAll(resolveNames(cCachePut.cacheNames(), cCachePut.value(), defaultNames));

        // @CacheEvict 추출
        CacheEvict mCacheEvict = AnnotatedElementUtils.findMergedAnnotation(method, CacheEvict.class);
        CacheEvict cCacheEvict = (mCacheEvict == null) ? AnnotatedElementUtils.findMergedAnnotation(targetClass, CacheEvict.class) : null;
        if (mCacheEvict != null) names.addAll(resolveNames(mCacheEvict.cacheNames(), mCacheEvict.value(), defaultNames));
        if (cCacheEvict != null) names.addAll(resolveNames(cCacheEvict.cacheNames(), cCacheEvict.value(), defaultNames));

        // @Caching 추출 (복합 어노테이션)
        Caching mCaching = AnnotatedElementUtils.findMergedAnnotation(method, Caching.class);
        Caching cCaching = (mCaching == null) ? AnnotatedElementUtils.findMergedAnnotation(targetClass, Caching.class) : null;
        processCaching(mCaching, names, defaultNames);
        processCaching(cCaching, names, defaultNames);

        return names.stream()
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .toList();
    }

    private void processCaching(final Caching caching, final Set<String> names, final String[] defaultNames) {
        if (caching == null) return;
        for (Cacheable c : caching.cacheable()) names.addAll(resolveNames(c.cacheNames(), c.value(), defaultNames));
        for (CachePut p : caching.put()) names.addAll(resolveNames(p.cacheNames(), p.value(), defaultNames));
        for (CacheEvict e : caching.evict()) names.addAll(resolveNames(e.cacheNames(), e.value(), defaultNames));
    }

    private List<String> resolveNames(final String[] cacheNames, final String[] value, final String[] defaultNames) {
        if (cacheNames.length > 0) return Arrays.asList(cacheNames);
        if (value.length > 0) return Arrays.asList(value);
        return Arrays.asList(defaultNames);
    }

    // 각 캐시별 서킷 브레이커 실행 상태를 담는 불변 객체
    private record CircuitBreakerExecution(String cacheName, CircuitBreaker circuitBreaker) {
    }
}
