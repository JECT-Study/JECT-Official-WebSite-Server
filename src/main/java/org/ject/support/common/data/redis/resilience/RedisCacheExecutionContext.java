package org.ject.support.common.data.redis.resilience;

import java.util.HashSet;
import java.util.Set;

public final class RedisCacheExecutionContext {

    // Per-request flag: true means cache access should be bypassed for current thread.
    private static final ThreadLocal<Boolean> CACHE_BYPASS = ThreadLocal.withInitial(() -> false);
    // Per-request set of cache names that failed due to redis issues.
    private static final ThreadLocal<Set<String>> FAILED_CACHES = ThreadLocal.withInitial(HashSet::new);

    private RedisCacheExecutionContext() {
    }

    // Called by aspect when any related circuit breaker is OPEN.
    public static void enableBypass() {
        CACHE_BYPASS.set(true);
    }

    // Read by CacheResolver to decide between real cache and NoOpCache.
    public static boolean isBypassEnabled() {
        return CACHE_BYPASS.get();
    }

    // Called by CacheErrorHandler when redis call fails for this cache.
    public static void markFailure(final String cacheName) {
        FAILED_CACHES.get().add(cacheName);
    }

    // Read by aspect to avoid marking breaker success on failed cache calls.
    public static boolean hasFailure(final String cacheName) {
        return FAILED_CACHES.get().contains(cacheName);
    }

    // Must be called in finally block to prevent ThreadLocal memory/state leak.
    public static void clear() {
        CACHE_BYPASS.remove();
        FAILED_CACHES.remove();
    }
}

