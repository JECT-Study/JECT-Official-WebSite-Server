package org.ject.support.common.data.redis.resilience;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class RedisCacheExecutionContext {

    // 중첩된 캐시 호출 시 상태를 보존하기 위해 스택(Deque) 구조를 사용
    private static final ThreadLocal<Deque<Boolean>> BYPASS_STACK = ThreadLocal.withInitial(ArrayDeque::new);
    // 요청별 레디스 장애가 발생한 캐시 이름들의 집합 (동일 스레드 내 모든 호출 공유)
    private static final ThreadLocal<Set<String>> FAILED_CACHES = ThreadLocal.withInitial(HashSet::new);

    // 캐시 호출 시작 시 현재 상태(우회 여부)를 스택에 추가
    public static void pushContext(final boolean bypassEnabled) {
        BYPASS_STACK.get().push(bypassEnabled);
    }

    // 캐시 호출 종료 시 이전 상태로 복구
    public static void popContext() {
        Deque<Boolean> stack = BYPASS_STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    // 현재 실행 컨텍스트에서 캐시 우회가 활성화되어 있는지 확인
    public static boolean isBypassEnabled() {
        Boolean bypass = BYPASS_STACK.get().peek();
        return bypass != null && bypass;
    }

    // 특정 캐시에 대한 레디스 호출이 실패했을 때 CacheErrorHandler에 의해 호출
    public static void markFailure(final String cacheName) {
        FAILED_CACHES.get().add(cacheName);
    }

    // 실패한 캐시 호출에 대해 서킷 브레이커가 성공을 기록하지 않도록 Aspect에서 참조
    public static boolean hasFailure(final String cacheName) {
        return FAILED_CACHES.get().contains(cacheName);
    }

    // 현재 활성화된 실행 컨텍스트(스택에 쌓인 호출)가 있는지 확인
    public static boolean hasContext() {
        return !BYPASS_STACK.get().isEmpty();
    }

    // ThreadLocal 메모리 누수 및 상태 오염을 방지하기 위해 최상위 호출이 끝날 때 호출
    public static void clear() {
        BYPASS_STACK.remove();
        FAILED_CACHES.remove();
    }
}

