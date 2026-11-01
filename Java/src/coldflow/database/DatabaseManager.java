package coldflow.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
private static final String HOST = "aws-0-ap-south-1.pooler.supabase.com";
private static final String PORT = "5432";
private static final String DATABASE = "postgres";
private static final String USER = "postgres.eufylkiyaeanrrvggnds";
private static final String PASSWORD = "Srk@2507@@@";

private static final String URL =
        "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?sslmode=require";

public static Connection getConnection() throws SQLException {

    return DriverManager.getConnection(
            URL,
            USER,
            PASSWORD
    );
}

public static void testConnection() {

    System.out.println("================================");
    System.out.println(" COLDFLOW DATABASE CONNECTION");
    System.out.println("================================");

    try (Connection connection = getConnection()) {

        System.out.println("Connected to Supabase successfully!");

    } catch (SQLException e) {

        System.out.println("Database connection failed!");
        System.out.println("Error: " + e.getMessage());
    }

    System.out.println("================================");
}

}
