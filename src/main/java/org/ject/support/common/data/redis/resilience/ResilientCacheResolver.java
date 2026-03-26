package org.ject.support.common.data.redis.resilience;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.NoOpCache;
import org.springframework.lang.NonNull;

public class ResilientCacheResolver implements CacheResolver {

    // Delegates actual cache lookup to Spring's configured CacheManager.
    private final CacheManager cacheManager;

    public ResilientCacheResolver(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    @NonNull
    public Collection<? extends Cache> resolveCaches(@NonNull final CacheOperationInvocationContext<?> context) {
        // Cache names declared on the current cache operation (@Cacheable/@CachePut/...)
        Collection<String> cacheNames = context.getOperation().getCacheNames();
        // Resolve in-order so behavior matches Spring default resolver semantics.
        List<Cache> resolvedCaches = new ArrayList<>(cacheNames.size());

        for (String cacheName : cacheNames) {
            // When bypass is enabled, return NoOpCache to skip all redis calls.
            if (RedisCacheExecutionContext.isBypassEnabled()) {
                resolvedCaches.add(new NoOpCache(cacheName));
                continue;
            }

            // Normal path: resolve real cache (RedisCache) from manager.
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                resolvedCaches.add(cache);
            }
            // If cache is missing, ignore it to follow Spring's permissive behavior.
        }

        return resolvedCaches;
    }
}
