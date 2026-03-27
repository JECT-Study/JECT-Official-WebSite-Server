package org.ject.support.common.data.redis.resilience;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.NoOpCache;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class ResilientCacheResolver implements CacheResolver {

    // 실제 캐시 조회 업무를 스프링에 설정된 CacheManager에 위임합니다.
    private final CacheManager cacheManager;

    @Override
    @NonNull
    public Collection<? extends Cache> resolveCaches(@NonNull final CacheOperationInvocationContext<?> context) {
        // 현재 캐시 작업(@Cacheable, @CachePut 등)에 선언된 캐시 이름 추출
        Collection<String> cacheNames = context.getOperation().getCacheNames();
        // 스프링의 기본 리졸버와 동일한 동작 방식을 위해 순서대로 리졸빙
        List<Cache> resolvedCaches = new ArrayList<>(cacheNames.size());

        for (String cacheName : cacheNames) {
            // 서킷 브레이커에 의해 우회(bypass)가 활성화된 경우, 모든 레디스 호출을 건너뛰기 위해 NoOpCache를 반환
            if (RedisCacheExecutionContext.isBypassEnabled()) {
                resolvedCaches.add(new NoOpCache(cacheName));
                continue;
            }

            // 캐시 매니저로부터 실제 캐시(RedisCache)를 찾아 반환
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                resolvedCaches.add(cache);
            }
            // 캐시를 찾을 수 없는 경우, 스프링의 기본 permissive 동작 방식을 따르기 위해 무시
        }

        return resolvedCaches;
    }
}
