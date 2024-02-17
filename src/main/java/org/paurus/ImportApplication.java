package org.paurus;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;

public class ImportApplication {
    @SneakyThrows
    public static void main(String[] args) {
        String url = "jdbc:h2:~/test";
        try (Connection conn = DriverManager.getConnection(url, "sa", ""); Statement statement = conn.createStatement()) {
            initializeDB(statement);

            String insertQuery = "INSERT INTO DATA (match_id, market_id, outcome_id, specifiers)" + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {
                insertDataInDB(preparedStatement);
            }

            queryDB(statement);
        }
    }

    private static void initializeDB(Statement statement) throws SQLException {
        System.out.println("Starting DB initialization");

        String createStatement = """
                DROP TABLE IF EXISTS DATA;
                CREATE TABLE DATA (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    match_id VARCHAR(100) NOT NULL,
                    market_id INT NOT NULL,
                    outcome_id VARCHAR(50) NOT NULL,
                    specifiers VARCHAR(1000),
                    date_insert TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
        statement.executeUpdate(createStatement);

        System.out.println("Completed DB initialization");
    }

    @SneakyThrows
    private static void insertDataInDB(PreparedStatement statement) {
        System.out.println("Starting data insertion");

        BufferedReader br = new BufferedReader(new InputStreamReader(ImportApplication.class.getResourceAsStream("/fo_random.txt")));
        String st;
        br.readLine();
        while ((st = br.readLine()) != null) {
            String[] split = st.split("\\|");
            statement.setString(1, split[0]);
            statement.setInt(2, Integer.parseInt(split[1]));
            statement.setString(3, split[2]);
            if (split.length > 3) {
                statement.setString(4, split[3]);
            } else {
                statement.setNull(4, java.sql.Types.NULL);
            }
            statement.execute();
            statement.clearParameters();
        }

        System.out.println("Completed data insertion");
    }

    @SneakyThrows
    private static void queryDB(Statement statement) {
        System.out.println("Starting data evaluation");

        try (ResultSet resultSetMin = statement.executeQuery("SELECT MIN(date_insert) AS min_insert_date FROM DATA")) {
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
