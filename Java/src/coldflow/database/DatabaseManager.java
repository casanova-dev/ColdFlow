package coldflow.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection manager for the ColdFlow Supabase database.
 *
 * Database credentials are read from environment variables so that they
 * are not stored in source code or committed to version control.
 */
public final class DatabaseManager {

    private static final String HOST_ENV = "COLDFLOW_DB_HOST";
    private static final String PORT_ENV = "COLDFLOW_DB_PORT";
    private static final String NAME_ENV = "COLDFLOW_DB_NAME";
    private static final String USER_ENV = "COLDFLOW_DB_USER";
    private static final String PASSWORD_ENV = "COLDFLOW_DB_PASSWORD";

    private DatabaseManager() {
        // Utility class; do not instantiate.
    }

    /**
     * Opens and returns a new JDBC connection to Supabase.
     * The caller is responsible for closing the returned connection.
     *
     * @return an open PostgreSQL connection
     * @throws SQLException if configuration is missing or connection fails
     */
    public static Connection getConnection() throws SQLException {

        String host = getRequiredEnvironmentVariable(HOST_ENV);
        String port = getRequiredEnvironmentVariable(PORT_ENV);
        String database = getRequiredEnvironmentVariable(NAME_ENV);
        String user = getRequiredEnvironmentVariable(USER_ENV);
        String password = getRequiredEnvironmentVariable(PASSWORD_ENV);

        String url = "jdbc:postgresql://" + host + ":" + port + "/"
                + database + "?sslmode=require";

        return DriverManager.getConnection(url, user, password);
    }

    private static String getRequiredEnvironmentVariable(String name)
            throws SQLException {

        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            throw new SQLException(
                    "Required environment variable is not configured: " + name
            );
        }

        return value.trim();
    }

    /**
     * Tests the Supabase connection and prints a concise result.
     */
    public static void testConnection() {

        System.out.println("================================");
        System.out.println(" COLDFLOW DATABASE CONNECTION");
        System.out.println("================================");

        try (Connection connection = getConnection()) {

            if (connection.isValid(5)) {
                System.out.println("Connected to Supabase successfully!");
            } else {
                System.out.println("Database connection could not be validated.");
            }

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("================================");
    }
}
