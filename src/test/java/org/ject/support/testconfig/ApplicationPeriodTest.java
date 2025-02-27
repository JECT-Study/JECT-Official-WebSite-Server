package org.ject.support.testconfig;

import static org.mockito.Mockito.when;

import org.ject.support.domain.recruit.dto.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class ApplicationPeriodTest {
    @MockitoBean
    protected RedisTemplate<String, String> redisTemplate;
    @Mock
    protected ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Constants.PERIOD_FLAG)).thenReturn(Boolean.toString(true));
    }
}
