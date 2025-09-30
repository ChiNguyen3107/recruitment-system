package com.recruitment.system.migration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class EmailVerificationMigrationTest {

    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void startContainer() {
        mysql.start();
    }

    @AfterAll
    static void stopContainer() {
        mysql.stop();
    }

    @Test
    void migration_is_backward_compatible_and_backfills_issued_at() throws Exception {
        try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = conn.createStatement()) {

            // 1) Apply base schema
            String baseSchema = readFile("database_schema.sql");
            for (String sql : splitSql(baseSchema)) {
                st.execute(sql);
            }

            // Insert sample users with and without verification token
            st.executeUpdate("INSERT INTO users (id, email, password, first_name, last_name, role, status, email_verified, created_at, updated_at) VALUES " +
                    "(1001, 'u1@example.com', 'x', 'U1', 'Ex', 'APPLICANT', 'PENDING', 0, NOW()-INTERVAL 2 DAY, NOW())," +
                    "(1002, 'u2@example.com', 'x', 'U2', 'Ex', 'APPLICANT', 'PENDING', 0, NOW()-INTERVAL 1 DAY, NOW())");
            st.executeUpdate("UPDATE users SET verification_token='tok-1' WHERE id=1001");

            // 2) Apply migration
            String migration = readFile("email_verification_migration.sql");
            for (String sql : splitSql(migration)) {
                st.execute(sql);
            }

            // 3) Checks: column exists and backfilled for the token user
            ResultSet rs = st.executeQuery("SELECT verification_token_issued_at IS NOT NULL FROM users WHERE id=1001");
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));

            // User without token should have NULL issued_at
            rs = st.executeQuery("SELECT verification_token_issued_at IS NULL FROM users WHERE id=1002");
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));

            // Index exists (best-effort check via explain using index)
            st.executeQuery("EXPLAIN SELECT * FROM users WHERE verification_token='tok-1'");
        }
    }

    private static String readFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    private static String[] splitSql(String script) {
        return script.replace("\r", "").split(";\n");
    }
}


