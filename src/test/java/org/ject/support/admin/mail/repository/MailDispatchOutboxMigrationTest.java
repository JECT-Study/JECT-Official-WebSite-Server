package org.ject.support.admin.mail.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class MailDispatchOutboxMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Test
    @DisplayName("MySQL TEXT byte limit을 넘는 본문을 Outbox migration에 저장한다")
    void MySQL_TEXT_byte_limit을_넘는_본문을_Outbox에_저장한다() throws SQLException {
        // given
        String body = "a".repeat(65_536);

        try (Connection connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE mail_dispatch_job (id BIGINT NOT NULL PRIMARY KEY) ENGINE=InnoDB");
                statement.execute("INSERT INTO mail_dispatch_job (id) VALUES (1)");
            }
            ScriptUtils.executeSqlScript(connection, new EncodedResource(
                    new ClassPathResource("db/migration/V46__create_mail_dispatch_outbox.sql")));

            // when
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO mail_dispatch_outbox (dispatch_job_id, apply_id, email, subject, body, status)
                    VALUES (1, 2, ?, ?, ?, 'PENDING')
                    """)) {
                statement.setString(1, "applicant@ject.kr");
                statement.setString(2, "안내");
                statement.setString(3, body);
                assertThat(statement.executeUpdate()).isOne();
            }

            // then
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT body FROM mail_dispatch_outbox WHERE dispatch_job_id = 1 AND apply_id = 2");
                 ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("body")).isEqualTo(body);
            }
        }
    }
}
