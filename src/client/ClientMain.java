package client;

import java.sql.Connection;
import javax.swing.SwingUtilities;

import dao.ServerDAO;
import database.DatabaseConnection;
import model.Server;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // Kết nối đến cơ sở dữ liệu
            Connection connection = DatabaseConnection.getConnection();
            if (connection == null) {
                System.err.println("Failed to connect to the database.");
                return;
            }

            // Khởi tạo ServerDAO
            ServerDAO serverDAO = new ServerDAO(connection);

            // Lấy thông tin server từ cơ sở dữ liệu
            Server server = serverDAO.getServerIpAndPort();
            if (server == null) {
                System.err.println("Server information not found in the database.");
                return;
            }

            // Khởi tạo MailClient với thông tin server
            MailClient mailClient = new MailClient(server.getServerIp(), server.getUdpPort(), server.getTcpPort());

            // Hiển thị giao diện đăng nhập
            SwingUtilities.invokeLater(() -> new LoginView(serverDAO, mailClient));

        } catch (Exception e) {
            System.err.println("Error initializing client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
