package org.paurus;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ImportApplicationWithDelay {
    private static final Map<Integer, Long> PROCESSING_DELAY_IN_MS = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> THREAD_POOL = new ConcurrentHashMap<>();

    private static final Random RANDOM = new Random();

    @SneakyThrows
    public static void main(String[] args) {
        String url = "jdbc:h2:~/test";

        Connection conn = DriverManager.getConnection(url, "sa", "");
        Statement statement = conn.createStatement();
        initializeDB(statement);

        processData(conn);

        // Wait for all task executors to finish
        while (true) {
            AtomicInteger executorFinishedCount = new AtomicInteger();
            THREAD_POOL.forEach((matchId, executor) -> {
                executor.shutdown();
                try {
                    executor.awaitTermination(1000, TimeUnit.SECONDS);
                    executorFinishedCount.getAndIncrement();
                } catch (InterruptedException e) {
                    System.out.println("Could not stop in time");
                }
            });
            if (executorFinishedCount.get() == THREAD_POOL.size()) {
                break;
            }
        }
        queryDB(statement);
    }

    private static void processData(Connection connection) throws IOException {
        System.out.println("Starting data processing");

        BufferedReader br = new BufferedReader(new InputStreamReader(ImportApplicationWithDelay.class.getResourceAsStream("/fo_random.txt")));
        br.readLine();
        String st;
        while ((st = br.readLine()) != null) {
            String finalSt = st;

            ExecutorService executor = THREAD_POOL.computeIfAbsent(st.split("\\|")[0], x -> Executors.newSingleThreadExecutor(Thread.ofVirtual().factory()));
            executor.execute(() -> processString(finalSt, connection));
        }
        System.out.println("Completed data insertion");
    }

    @SneakyThrows
    private static void processString(String st, Connection connection) {
        String[] split = st.split("\\|");
        int eventType = Integer.parseInt(split[1]);
        Long processingDelay = PROCESSING_DELAY_IN_MS.computeIfAbsent(eventType, x -> RANDOM.nextLong(50L));
        System.out.printf("Processing %s with delay %d%n", split[0], processingDelay);

        Thread.sleep(processingDelay);

        String insertQuery = "INSERT INTO DATA (match_id, market_id, outcome_id, specifiers)" + "VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        insertDataInDB(preparedStatement, split[0], eventType, split[2], split.length > 3 ? split[3] : null);
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
    private static void insertDataInDB(PreparedStatement statement, String st, int eventType, String s, String specifiers) {
        statement.setString(1, st);
        statement.setInt(2, eventType);
        statement.setString(3, s);
        if (specifiers != null) {
            statement.setString(4, specifiers);
        } else {
            statement.setNull(4, Types.NULL);
        }
        statement.execute();
        statement.clearParameters();
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