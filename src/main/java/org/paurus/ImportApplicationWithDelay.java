package org.paurus;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ImportApplicationWithDelay {
    private static final Map<Integer, Long> PROCESSING_DELAY_IN_MS = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> THREAD_POOL = new HashMap<>();

    private static final Random RANDOM = new Random();

    @SneakyThrows
    public static void main(String[] args) {
        String url = "jdbc:h2:~/test";
        Connection conn = DriverManager.getConnection(url, "sa", "");
        Statement statement = conn.createStatement();

        dropExistingTableAndCreateNewTable(statement);

        processData(conn);

        waitForAllTaskExecutorsToCompleteProcessing();

        queryDBForMinAndMaxInsertionTimestamp(statement);
    }

    private static void dropExistingTableAndCreateNewTable(Statement statement) throws SQLException {
        log.info("Starting DB recreation");

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

        log.info("Completed DB recreation");
    }

    private static void processData(Connection connection) throws IOException {
        log.info("Starting file processing");

        BufferedReader br = new BufferedReader(new InputStreamReader(ImportApplicationWithDelay.class.getResourceAsStream("/fo_random.txt")));
        br.readLine();
        String st;
        while ((st = br.readLine()) != null) {
            // Find or create the executor service to schedule the line processing
            String matchId = st.split("\\|")[0];
            // Using newSingleThreadExecutor ensures threads are run sequentially for every matchId
            // Processing events from one match id should have no effect on other matchId
            ExecutorService executor = THREAD_POOL.computeIfAbsent(matchId, x -> Executors.newSingleThreadExecutor(Thread.ofVirtual().name(matchId).factory()));

            String finalSt = st;
            executor.execute(() -> processString(finalSt, connection));
        }
        log.info("Completed file processing");
    }

    @SneakyThrows
    private static void processString(String st, Connection connection) {
        String[] split = st.split("\\|");
        int eventType = Integer.parseInt(split[1]);
        // Set random delay to same value for events of the same type
        Long processingDelay = PROCESSING_DELAY_IN_MS.computeIfAbsent(eventType, x -> RANDOM.nextLong(50L));
        log.info("Processing {} with delay {}", split[0], processingDelay);

        Thread.sleep(processingDelay);

        String insertQuery = "INSERT INTO DATA (match_id, market_id, outcome_id, specifiers)" + "VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        insertDataInDB(preparedStatement, split[0], eventType, split[2], split.length > 3 ? split[3] : null);
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

    private static void waitForAllTaskExecutorsToCompleteProcessing() {
        log.info("Started waiting on all executors to complete");
        AtomicInteger executorFinishedCount = new AtomicInteger();
        THREAD_POOL.forEach((matchId, executor) -> {
            executor.shutdown();
            try {
                executor.awaitTermination(1000, TimeUnit.SECONDS);
                executorFinishedCount.getAndIncrement();
            } catch (InterruptedException e) {
                log.info("Could not stop in time");
            }

        });
        log.info("Completed waiting on all executors to complete");
    }

    @SneakyThrows
    private static void queryDBForMinAndMaxInsertionTimestamp(Statement statement) {
        log.info("Starting data evaluation");

        try (ResultSet resultSetMin = statement.executeQuery("SELECT MIN(date_insert) AS min_insert_date FROM DATA")) {
            if (resultSetMin.first()) {
                log.info("Minimum insertion date: " + resultSetMin.getTimestamp("min_insert_date"));
            }
        }

        try (ResultSet resultSetMax = statement.executeQuery("SELECT MAX(date_insert) AS max_insert_date FROM DATA")) {
            if (resultSetMax.first()) {
                log.info("Maximum insertion date: " + resultSetMax.getTimestamp("max_insert_date"));
            }
        }

        log.info("Completed data evaluation");
    }

}
