package org.ject.support.common.data.redis.resilience;

import java.util.Locale;
import java.util.concurrent.TimeoutException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;

public final class RedisCacheExceptionClassifier {

    // Utility class: prevent instantiation.
    private RedisCacheExceptionClassifier() {
    }

    // Walks throwable cause chain and decides whether failure is redis/network related.
    public static boolean isRedisRelated(final Throwable throwable) {
        // Start from top-level exception thrown by cache infrastructure.
        Throwable cursor = throwable;

        // Inspect whole cause chain because wrappers differ by driver/spring version.
        while (cursor != null) {
            // Known redis/timeout exception types handled explicitly.
            if (cursor instanceof RedisConnectionFailureException
                    || cursor instanceof RedisSystemException
                    || cursor instanceof QueryTimeoutException
                    || cursor instanceof TimeoutException) {
                return true;
            }

            // Defensive fallback for wrapped vendor-specific exception names.
            String className = cursor.getClass().getName().toLowerCase(Locale.ROOT);
            if (className.contains("redis") || className.contains("lettuce")) {
                return true;
            }

            // Move down to nested cause.
            cursor = cursor.getCause();
        }

        // Not a redis-related failure.
        return false;
    }
}

