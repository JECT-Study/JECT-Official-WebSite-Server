package org.ject.support.domain.applicant.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.ject.support.base.TestSupport;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class ApplicantRecruitMigrationTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Test
    void 기존_지원_이력을_보존하며_공고별_지원자_식별과_활성_이메일_유일성을_적용한다() throws SQLException {
        // given
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .target("56").load().migrate();
        try (Connection connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO semester (id, semester_name, is_recruiting) VALUES (1, '검증기수', 1)");
            statement.execute("""
                    INSERT INTO recruit (id, semester_id, job_family, start_date, end_date,
                                         recruit_type, recruit_type_detail, faqs, status)
                    VALUES (1, 1, 'BE', '2026-01-01', '2026-12-31', 'SEMESTER', 'REGULAR', '[]', 'OPEN'),
                           (2, NULL, 'BE', '2026-01-01', '2026-12-31', 'MAKERS', 'NEW', '[]', 'OPEN')
                    """);
            statement.execute("""
                    INSERT INTO applicant (id, email, semester_id, role, is_deleted)
                    VALUES (1, 'same@ject.kr', 1, 'APPLY', 0),
                           (2, 'same@ject.kr', 1, 'APPLY', 0),
                           (3, 'noapply@ject.kr', 1, 'APPLY', 0),
                           (4, 'multiple@ject.kr', 1, 'APPLY', 0),
                           (5, 'admin@ject.kr', 1, 'ADMIN', 0),
                           (6, 'same@ject.kr', 1, 'APPLY', 1)
                    """);
            statement.execute("""
                    INSERT INTO apply (id, applicant_id, recruit_id, status)
                    VALUES (1, 1, 1, 'SUBMITTED'), (2, 2, 2, 'SUBMITTED'),
                           (3, 4, 1, 'SUBMITTED'), (4, 4, 2, 'JOINED'),
                           (5, 5, 1, 'SUBMITTED'), (6, 6, 1, 'SUBMITTED')
                    """);

            // when
            Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                    .load().migrate();

            // then
            // 기수 데이터는 유지하고 모집 플래그 컬럼만 제거됐는지 확인
            try (ResultSet columns = connection.getMetaData().getColumns(
                    connection.getCatalog(), null, "semester", "is_recruiting")) {
                assertThat(columns.next()).isFalse();
            }
            try (ResultSet rows = statement.executeQuery("SELECT semester_name FROM semester WHERE id = 1")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("semester_name")).isEqualTo("검증기수");
            }
            assertThat(statement.executeUpdate(
                    "INSERT INTO semester (semester_name) VALUES ('신규기수')")).isEqualTo(1);
            try (ResultSet rows = statement.executeQuery(
                    "SELECT id, recruit_id, semester_id, member_type FROM applicant ORDER BY id")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getLong("recruit_id")).isEqualTo(1);
                assertThat(rows.getLong("semester_id")).isEqualTo(1);
                assertThat(rows.getString("member_type")).isEqualTo("SEMESTER");
                assertThat(rows.next()).isTrue();
                assertThat(rows.getLong("recruit_id")).isEqualTo(2);
                assertThat(rows.getObject("semester_id")).isNull();
                assertThat(rows.getString("member_type")).isEqualTo("MAKERS");
                for (int id = 3; id <= 5; id++) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getObject("recruit_id")).isNull();
                }
                assertThat(rows.next()).isTrue();
                assertThat(rows.getLong("recruit_id")).isEqualTo(1);
            }
            assertThatThrownBy(() -> statement.execute("""
                    INSERT INTO applicant (email, recruit_id, semester_id, role)
                    VALUES ('same@ject.kr', 1, 1, 'APPLY')
                    """))
                    .isInstanceOf(SQLException.class)
                    .satisfies(error -> assertThat(((SQLException) error).getSQLState()).startsWith("23"));
            assertThat(statement.executeUpdate("""
                    INSERT INTO applicant (email, recruit_id, semester_id, role, is_deleted)
                    VALUES ('same@ject.kr', 1, 1, 'APPLY', 1), ('same@ject.kr', 1, 1, 'APPLY', 1)
                    """)).isEqualTo(2);
            try (ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM apply")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt(1)).isEqualTo(6);
            }
        }
    }
}
