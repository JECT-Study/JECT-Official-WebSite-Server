package org.ject.support.admin.mail.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.ject.support.base.TestSupport;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(QueryDslTestConfig.class)
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class MailDispatchOutboxMigrationTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Test
    @DisplayName("V44 기준선에서 Flyway로 후속 메일 migration 적용 후 기존 작업과 Outbox를 검증한다")
    void V44_기준선에서_후속_메일_migration_적용_후_기존_작업과_Outbox를_검증한다() throws SQLException {
        // given
        String body = "a".repeat(65_536);

        try (Connection connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE mail_dispatch_job (
                            id BIGINT NOT NULL PRIMARY KEY,
                            input_variables_json TEXT
                        ) ENGINE=InnoDB
                        """);
                statement.execute("INSERT INTO mail_dispatch_job (id) VALUES (1)");
            }

            Flyway flyway = Flyway.configure()
                    .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                    .baselineVersion("44")
                    .load();
            flyway.baseline();
            flyway.migrate();

            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT COLUMN_NAME
                    FROM INFORMATION_SCHEMA.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'mail_dispatch_outbox'
                      AND INDEX_NAME = 'idx_mail_dispatch_outbox_status_id'
                    ORDER BY SEQ_IN_INDEX
                    """)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.getString("COLUMN_NAME")).isEqualTo("status");
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.getString("COLUMN_NAME")).isEqualTo("id");
                    assertThat(resultSet.next()).isFalse();
                }
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT request_fingerprint, unknown_count, claim_started_at FROM mail_dispatch_job WHERE id = 1")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("request_fingerprint")).isNull();
                assertThat(resultSet.getInt("unknown_count")).isZero();
                assertThat(resultSet.getTimestamp("claim_started_at")).isNull();
            }

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

            assertThatThrownBy(() -> {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO mail_dispatch_outbox (dispatch_job_id, apply_id, email, subject, body, status)
                        VALUES (1, 2, 'applicant@ject.kr', '안내', '중복 대상', 'PENDING')
                        """)) {
                    statement.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
                    .satisfies(exception -> assertThat(((SQLException) exception).getSQLState())
                            .startsWith("23"));
        }
    }
}
