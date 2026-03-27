package org.ject.support.common.data.redis.resilience;

import java.util.concurrent.TimeoutException;
import lombok.experimental.UtilityClass;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;

@UtilityClass
public final class RedisCacheExceptionClassifier {

    // 예외의 원인 체인(Cause Chain)을 탐색하여 레디스 인프라 장애(연결, 타임아웃 등)인지 판단합니다.
    public static boolean isRedisRelated(final Throwable throwable) {
        Throwable cursor = throwable;

        while (cursor != null) {
            // 명시적으로 처리되는 레디스/네트워크 타임아웃 예외 타입
            if (cursor instanceof RedisConnectionFailureException
                    || cursor instanceof RedisSystemException
                    || cursor instanceof QueryTimeoutException
                    || cursor instanceof TimeoutException) {
                return true;
            }

            // 라이브러리(Lettuce 등) 전용 하위 예외나 네트워크 관련 키워드를 포함하는 경우만 인프라 장애로 간주
            // SerializationException과 같이 비인프라(데이터/코드)성 예외는 포함되지 않도록 범위 설정
            String className = cursor.getClass().getName();
            if (className.contains("io.lettuce.core") || className.contains("RedisCommand") || className.contains("RedisConnection")) {
                return true;
            }

            cursor = cursor.getCause();
        }

        return false;
    }
}

