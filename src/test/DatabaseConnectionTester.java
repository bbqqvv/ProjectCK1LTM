package test;

import java.sql.Connection;
import java.sql.SQLException;
import database.DatabaseConnection;

public class DatabaseConnectionTester {
    
    public static void testConnection() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connection is active.");
            } else {
                System.out.println("Failed to make connection.");
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();  // Đóng kết nối sau khi kiểm tra
                    System.out.println("Connection closed.");
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}
