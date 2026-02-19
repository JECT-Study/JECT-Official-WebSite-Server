package org.ject.support.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MySQLContainer;

@Profile("test")
@TestConfiguration
public class MysqlTestContainersConfig {
    private static final int PORT = 3306;

    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.2")
            .withExposedPorts(PORT);

    static {
        if(!mysqlContainer.isRunning()){
            mysqlContainer.start();
        }
    }



}
