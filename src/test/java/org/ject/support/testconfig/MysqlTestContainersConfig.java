package org.ject.support.testconfig;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Profile("test")
@TestConfiguration
@Testcontainers
public class MysqlTestContainersConfig implements DisposableBean {
    private static final int PORT = 3306;

    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.2")
            .withExposedPorts(PORT);

    static {
        if(!mysqlContainer.isRunning()){
            mysqlContainer.start();
        }
    }


    @Override
    public void destroy() {
        if(mysqlContainer.isRunning()){
            mysqlContainer.stop();
        }
    }
}
