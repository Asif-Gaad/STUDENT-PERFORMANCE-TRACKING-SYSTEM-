package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Singleton DatabaseConnection class.
 * Manages a single shared MySQL connection for the application.
 */
public class DatabaseConnection {

    // ── Configuration ──────────────────────────────────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "student_performance_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";   // XAMPP default is empty

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    // ── Singleton instance ──────────────────────────────────────────────────────
    private static DatabaseConnection instance;
    private Connection connection;

    /** Private constructor – loads driver and opens connection. */
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            showError("MySQL JDBC Driver not found.\n"
                    + "Please add mysql-connector-java-8.x.jar to your project libraries.", e);
        } catch (SQLException e) {
            showError("Cannot connect to the database.\n"
                    + "Make sure XAMPP MySQL service is running.", e);
        }
    }

    /**
     * Returns the singleton instance, creating it if necessary.
     * @return DatabaseConnection singleton
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the active java.sql.Connection.
     * Reconnects automatically if the connection has been closed.
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            showError("Failed to re-establish database connection.", e);
        }
        return connection;
    }

    /** Closes the connection and resets the singleton. */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                instance = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
