package com.aarsoma.deliverynote.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBConfig {
    private static final String DB_URL = "jdbc:h2:./data/aarsoma;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    // Simple connection pooling
    private static final int MAX_POOL_SIZE = 10;
    private static final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();

    // Initialize the database and create tables if they don't exist
    public static void initDatabase() {
        try {
            // Test connection and create tables
            try (Connection conn = getConnection()) {
                createTablesIfNotExist(conn);
            }

            // Pre-populate the connection pool
            for (int i = 0; i < MAX_POOL_SIZE / 2; i++) {
                Connection conn = createConnection();
                if (conn != null) {
                    connectionPool.offer(conn);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    // Create a new connection
    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Get a connection from the pool or create a new one if the pool is empty
    public static Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }

        // Test the connection to ensure it's valid
        if (!connection.isValid(2)) { // 2 seconds timeout
            connection.close();
            connection = createConnection();
        }

        return connection;
    }

    // Return a connection to the pool
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && connection.isValid(2)) {
                    if (connectionPool.size() < MAX_POOL_SIZE) {
                        // Reset connection state before returning to pool
                        connection.setAutoCommit(true);
                        connectionPool.offer(connection);
                    } else {
                        connection.close();
                    }
                } else {
                    // If the connection is invalid or closed, discard it
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                // Log error and discard connection
                System.err.println("Error releasing connection: " + e.getMessage());
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    // Ignore
                }
            }
        }
    }

    // Close all connections when application is shutting down
    public static void closeConnections() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Just log the error and continue
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Create database tables if they don't exist
    private static void createTablesIfNotExist(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Customers table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS customers (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "address VARCHAR(200), " +
                            "contact_person VARCHAR(100), " +
                            "phone VARCHAR(20), " +
                            "email VARCHAR(100), " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                            ")"
            );

            // Delivery Notes table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS delivery_notes (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "note_number VARCHAR(20) NOT NULL UNIQUE, " +
                            "customer_id INT NOT NULL, " +
                            "issue_date TIMESTAMP NOT NULL, " +
                            "financial_year VARCHAR(9) NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (customer_id) REFERENCES customers(id) " +
                            ")"
            );

            // Delivery Items table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS delivery_items (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "delivery_note_id INT NOT NULL, " +
                            "item_name VARCHAR(100) NOT NULL, " +
                            "ordered_qty INT NOT NULL, " +
                            "delivered_qty INT NOT NULL, " +
                            "balance_qty INT NOT NULL, " +
                            "FOREIGN KEY (delivery_note_id) REFERENCES delivery_notes(id) " +
                            ")"
            );
        }
    }
}
