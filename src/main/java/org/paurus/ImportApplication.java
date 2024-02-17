package org.paurus;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

public class ImportApplication {
    @SneakyThrows
    public static void main(String[] args) {
        String url = "jdbc:h2:~/test";
        try (Connection conn = DriverManager.getConnection(url, "sa", ""); Statement statement = conn.createStatement()) {
            initializeDB(statement);
            insertDataInDB(statement);
            queryDB(statement);
        }
    }

    private static void initializeDB(Statement statement) throws SQLException {
        System.out.println("Starting DB initialization");

        String createStatement = """
                DROP TABLE IF EXISTS DATA;
                CREATE TABLE DATA (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    match_id INT NOT NULL,
                    market_id INT NOT NULL,
                    outcome_id VARCHAR(50) NOT NULL,
                    specifiers VARCHAR(1000) NOT NULL,
                    date_insert TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
        statement.executeUpdate(createStatement);

        System.out.println("Completed DB initialization");
    }

    @SneakyThrows
    private static void insertDataInDB(Statement statement) {
        System.out.println("Starting data insertion");

        String sql = "INSERT INTO DATA (match_id, market_id, outcome_id, specifiers)" + "VALUES (1, 1, '2', 'RANDOM TEXT')";
        statement.executeUpdate(sql);
        String sql2 = "INSERT INTO DATA (match_id, market_id, outcome_id, specifiers)" + "VALUES (2, 2, '3', 'RANDOM TEXT2')";
        statement.executeUpdate(sql2);

        System.out.println("Completed data insertion");
    }

    @SneakyThrows
    private static void queryDB(Statement statement) {
        System.out.println("Starting data evaluation");

        try(ResultSet resultSetMin = statement.executeQuery("SELECT MIN(date_insert) AS min_insert_date FROM DATA")) {
            if (resultSetMin.first()) {
                System.out.println("Minimum insertion date: " + resultSetMin.getTimestamp("min_insert_date"));
            }
        }

        try (ResultSet resultSetMax = statement.executeQuery("SELECT MAX(date_insert) AS max_insert_date FROM DATA")) {
            if (resultSetMax.first()) {
                System.out.println("Maximum insertion date: " + resultSetMax.getTimestamp("max_insert_date"));
            }
        }

        System.out.println("Completed data evaluation");
    }

}
