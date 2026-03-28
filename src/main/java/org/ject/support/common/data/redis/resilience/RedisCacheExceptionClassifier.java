package org.ject.support.common.data.redis.resilience;

import java.util.concurrent.TimeoutException;
import lombok.experimental.UtilityClass;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;

@UtilityClass
public final class RedisCacheExceptionClassifier {

    // 예외의 cause chain을 탐색하여 레디스 인프라 장애인지 판단
    public static boolean isRedisRelated(final Throwable throwable) {
        Throwable cursor = throwable;

        while (cursor != null) {
            if (cursor instanceof RedisConnectionFailureException
                    || cursor instanceof RedisSystemException
                    || cursor instanceof QueryTimeoutException
                    || cursor instanceof TimeoutException) {
                return true;
            }

            // 라이브러리 전용 예외나 관련 키워드를 포함하는 경우만 장애로 간주
            String className = cursor.getClass().getName();
            if (className.contains("io.lettuce.core")
                    || className.contains("RedisCommand")
                    || className.contains("RedisConnection")) {
                return true;
            }

            cursor = cursor.getCause();
        }

        return false;
    }
}

