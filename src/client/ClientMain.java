package client;

import java.sql.Connection;

import javax.swing.SwingUtilities;

import dao.ServerDAO;
import database.DatabaseConnection;

public class ClientMain {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection(); // Lấy kết nối
            SwingUtilities.invokeLater(() -> {
                ServerDAO serverDAO = new ServerDAO(connection); // Khởi tạo ServerDAO
            // Khởi tạo 	đối tượng MailClient sau khi đăng nhập thành công
            new LoginView(serverDAO); // Không cần cung cấp serverAddress ban đầu, sẽ lấy sau khi đăng nhập
            });
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
