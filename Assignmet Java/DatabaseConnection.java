import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/student_management?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";  // Change this to your MySQL username... by default its mine
    private static final String PASSWORD = "123456";      // Change this to your MySQL password... by default its mine

    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Attempt to connect to the database
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Successfully connected to the database!");
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Please add the MySQL Connector/J to your project.", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to the database. Please check your credentials and database status.", e);
        }
    }

    // Test the connection
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("Database connection test successful!");
            conn.close();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
        }
    }
} 