package client;

import java.sql.Connection;
import javax.swing.SwingUtilities;

import dao.ServerDAO;
import database.DatabaseConnection;
import model.Server;

public class ClientMain {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection(); // Kết nối đến cơ sở dữ liệu
            ServerDAO serverDAO = new ServerDAO(connection); // Khởi tạo ServerDAO

            // Lấy thông tin server từ cơ sở dữ liệu
            Server server = serverDAO.getServerIpAndPort();
            if (server == null) {
                System.err.println("Server information not found in the database.");
                return;
            }
            MailClient mailClient = new MailClient(server.getServerIp(), server.getUdpPort());

            SwingUtilities.invokeLater(() -> new LoginView(serverDAO, mailClient));
        } catch (Exception e) {
            System.err.println("Error initializing client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
