package dao;

import model.Server;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID; // Import UUID class

public class ServerDAO {
    private Connection connection;

    public ServerDAO(Connection connection) {
        this.connection = connection;
    }

    public void saveServer(String serverIp, int udpPort, int tcpPort) {
        if (serverExists(serverIp, udpPort)) {
            System.out.println("Server với IP " + serverIp + " và Port " + udpPort + " đã tồn tại.");
            return;
        }

        String uniqueId = UUID.randomUUID().toString();  // Tạo UUID

        String sql = "INSERT INTO server_config (server_id, server_ip, port_udp, port_tcp) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE port_udp = ?, port_tcp = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Thiết lập các tham số cho phần VALUES
            preparedStatement.setString(1, uniqueId);  // Thiết lập UUID dưới dạng String
            preparedStatement.setString(2, serverIp);
            preparedStatement.setInt(3, udpPort);
            preparedStatement.setInt(4, tcpPort);

            // Thiết lập các tham số cho phần ON DUPLICATE KEY UPDATE
            preparedStatement.setInt(5, udpPort);  // Cập nhật port_udp
            preparedStatement.setInt(6, tcpPort);  // Cập nhật port_tcp

            // Thực thi câu truy vấn
            preparedStatement.executeUpdate();
            System.out.println("Server đã được lưu thành công với IP " + serverIp + " và Port " + udpPort);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean serverExists(String serverIp, int port) {
        String sql = "SELECT COUNT(*) FROM server_config WHERE server_ip = ? AND port_udp = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, serverIp);
            preparedStatement.setInt(2, port);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking server existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteServer(String serverIp, int serverPort) {
        if (!serverExists(serverIp, serverPort)) {
            System.out.println("Server with IP " + serverIp + " and Port " + serverPort + " does not exist in the database.");
            return false;
        }

        String sql = "DELETE FROM server_config WHERE server_ip = ? AND port_udp = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, serverIp);
            preparedStatement.setInt(2, serverPort);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Server deleted successfully with IP " + serverIp + " and Port " + serverPort);
                return true;
            } else {
                System.out.println("Failed to delete server with IP " + serverIp + " and Port " + serverPort);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting server: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public Server getServerIpAndPort() {
        String sql = "SELECT server_ip, port_udp, port_tcp FROM server_config LIMIT 1"; // Lấy bản ghi đầu tiên
        Server server = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String serverIp = resultSet.getString("server_ip");
                int serverPortUdp= resultSet.getInt("port_udp");
                int serverPortTcp = resultSet.getInt("port_tcp");
                server = new Server(serverIp, serverPortUdp, serverPortTcp);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving server IP and port: " + e.getMessage());
            e.printStackTrace();
        }

        return server;
    }
}
