package org.ject.support.testconfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({RedisTestContainersConfig.class, MysqlTestContainersConfig.class})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
}
