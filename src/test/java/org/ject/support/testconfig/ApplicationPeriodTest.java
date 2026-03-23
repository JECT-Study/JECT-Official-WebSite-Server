package org.ject.support.testconfig;

import static org.mockito.Mockito.when;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@org.springframework.test.context.TestPropertySource(properties = {"spring.cache.type=none"})
public abstract class ApplicationPeriodTest {
    @MockitoBean
    protected RedisTemplate<String, String> redisTemplate;
    
    @MockitoBean
    protected RedisConnectionFactory redisConnectionFactory;
    
    @Mock
    protected ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, JobFamily.PM.name())))
                .thenReturn(Boolean.toString(true));
        when(valueOperations.get(String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, JobFamily.PD.name())))
                .thenReturn(Boolean.toString(true));
        when(valueOperations.get(String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, JobFamily.FE.name())))
                .thenReturn(Boolean.toString(true));
        when(valueOperations.get(String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, JobFamily.BE.name())))
                .thenReturn(Boolean.toString(true));
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
    }
}
