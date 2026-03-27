package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.Ordered;
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

    // @Cacheable, @CachePut, @CacheEvict, @Caching 어노테이션이 사용된 모든 메서드를 가로챕니다.
    @Around("@annotation(org.springframework.cache.annotation.Cacheable) || " +
            "@annotation(org.springframework.cache.annotation.CachePut) || " +
            "@annotation(org.springframework.cache.annotation.CacheEvict) || " +
            "@annotation(org.springframework.cache.annotation.Caching)")
    public Object aroundCacheCall(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 하나의 메서드는 여러 어노테이션을 통해 다수의 캐시 이름을 참조할 수 있습니다.
        List<String> cacheNames = extractCacheNames(method);
        // 이번 요청에서 실행 권한을 획득한 서킷 브레이커들을 추적합니다.
        List<CircuitBreakerExecution> grantedBreakers = new ArrayList<>(cacheNames.size());
        // 참조하는 캐시 중 하나라도 OPEN 상태면 이번 호출은 캐시를 우회합니다.
        boolean bypassEnabled = false;

        // 캐시별 서킷 브레이커로부터 실행 권한을 획득 시도합니다.
        for (String cacheName : cacheNames) {
            CircuitBreaker circuitBreaker = circuitBreakerProvider.get(cacheName);
            // 레디스 호출 방지를 위해 OPEN 상태는 즉시 권한을 거부합니다.(Fail-fast)
            if (!circuitBreaker.tryAcquirePermission()) {
                bypassEnabled = true;
                log.debug("Cache circuit is open. cache={}, method={}", cacheName, joinPoint.getSignature());
                continue;
            }

            // 메서드 실행 후 성공 여부를 기록하기 위해 획득한 서킷 브레이커를 보관합니다.
            grantedBreakers.add(new CircuitBreakerExecution(cacheName, circuitBreaker));
        }

        // ThreadLocal 우회 설정을 스택에 쌓아 중첩된 호출에서도 상태를 유지합니다.
        RedisCacheExecutionContext.pushContext(bypassEnabled);

        try {
            // 메서드 실행을 계속합니다. 우회가 활성화된 경우 캐시 레이어는 건너뜁니다.
            Object result = joinPoint.proceed();
            // 실제 레디스 캐시를 사용한 경우(우회되지 않은 경우)에만 성공을 기록합니다.
            if (!bypassEnabled) {
                recordSuccess(grantedBreakers);
            }
            return result;
        } finally {
            // 이번 호출의 컨텍스트를 제거하여 이전 호출 상태로 복구합니다.
            RedisCacheExecutionContext.popContext();
            // 최상위 호출이 종료될 때 전체 상태를 정리하여 메모리 누수를 방지합니다.
            if (!RedisCacheExecutionContext.hasContext()) {
                RedisCacheExecutionContext.clear();
            }
        }
    }

    // 이번 호출에서 레디스 장애가 발생하지 않은 캐시들에 대해서만 성공을 기록합니다.
    private void recordSuccess(final List<CircuitBreakerExecution> grantedBreakers) {
        for (CircuitBreakerExecution execution : grantedBreakers) {
            // 캐시 조회/저장 중 장애가 발생했다면, ErrorHandler에서 이미 실패로 처리했습니다.
            if (RedisCacheExecutionContext.hasFailure(execution.cacheName())) {
                continue;
            }
            // 단순 성공 여부만 필요하므로 지연 시간(duration)은 0으로 기록합니다.
            execution.circuitBreaker().onSuccess(0, TimeUnit.NANOSECONDS);
        }
    }

    // @Caching을 포함하여 메서드에 선언된 모든 캐시 관련 어노테이션에서 캐시 이름을 추출합니다.
    private List<String> extractCacheNames(final Method method) {
        List<String> names = new ArrayList<>();

        // @Cacheable 처리
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        if (cacheable != null) {
            names.addAll(getNames(cacheable.cacheNames(), cacheable.value()));
        }

        // @CachePut 처리
        CachePut cachePut = method.getAnnotation(CachePut.class);
        if (cachePut != null) {
            names.addAll(getNames(cachePut.cacheNames(), cachePut.value()));
        }

        // @CacheEvict 처리
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        if (cacheEvict != null) {
            names.addAll(getNames(cacheEvict.cacheNames(), cacheEvict.value()));
        }

        // @Caching 처리 (복합 어노테이션)
        Caching caching = method.getAnnotation(Caching.class);
        if (caching != null) {
            Stream.of(caching.cacheable())
                    .forEach(c -> names.addAll(getNames(c.cacheNames(), c.value())));
            Stream.of(caching.put())
                    .forEach(c -> names.addAll(getNames(c.cacheNames(), c.value())));
            Stream.of(caching.evict())
                    .forEach(c -> names.addAll(getNames(c.cacheNames(), c.value())));
        }

        // 클래스 레벨의 @CacheConfig가 있을 경우 기본 캐시 이름을 추가로 고려할 수 있으나,
        // 현재는 메서드 레벨의 명시적 선언을 우선순위로 하여 추출합니다.

        return names.stream()
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .toList();
    }

    private List<String> getNames(final String[] cacheNames, final String[] value) {
        if (cacheNames.length > 0) {
            return Arrays.asList(cacheNames);
        }
        return Arrays.asList(value);
    }

    // 각 캐시별 서킷 브레이커 실행 상태를 담는 불변 객체입니다.
    private record CircuitBreakerExecution(String cacheName, CircuitBreaker circuitBreaker) {
    }
}
