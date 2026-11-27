package org.ject.support.testconfig;

import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MySQLContainer;

public final class MysqlTestContainersConfig {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2")
            .withUsername("root");

    static {
        MYSQL.start();
        Runtime.getRuntime().addShutdownHook(new Thread(MYSQL::stop));
    }

    private MysqlTestContainersConfig() {
    }

    // 공유 MySQL 안에 테스트 클래스별 독립 DB 생성
    public static synchronized String createDatabase(String databaseName) {
        if (!databaseName.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid test database name");
        }
        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), getUsername(), getPassword());
             var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS `" + databaseName + "`");
        } catch (SQLException exception) {
            throw new IllegalStateException("테스트 DB 생성 실패", exception);
        }
        return "jdbc:mysql://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(3306) + "/" + databaseName;
    }

    // 테스트 전용 DB 연결과 MySQL 방언 등록
    public static void registerDataSource(DynamicPropertyRegistry registry, String jdbcUrl) {
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.datasource.username", MysqlTestContainersConfig::getUsername);
        registry.add("spring.datasource.password", MysqlTestContainersConfig::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    public static String getUsername() {
        return MYSQL.getUsername();
    }

    public static String getPassword() {
        return MYSQL.getPassword();
    }
}
