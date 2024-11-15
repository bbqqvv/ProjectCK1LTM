package dao;

import model.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ServerDAO {
    private Connection connection;

    public ServerDAO(Connection connection) {
        this.connection = connection;
    }

    public void saveServer(String serverIp, int serverPort) {
        String sql = "INSERT INTO server_config (server_id, server_ip, server_port) VALUES (?, ?, ?)";
        
        // Tạo ID ngẫu nhiên bằng UUID
        String serverId = UUID.randomUUID().toString();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, serverId); // ID
            preparedStatement.setString(2, serverIp); // IP
            preparedStatement.setInt(3, serverPort);  // Port
            
            // Thực thi câu lệnh
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteServer(String serverIp, int port) {
        String sql = "DELETE FROM server_config WHERE server_ip = ? AND server_port = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, serverIp);  // Địa chỉ IP
            preparedStatement.setInt(2, port);         // Số port
            
            // Thực thi câu lệnh xóa
            int rowsAffected = preparedStatement.executeUpdate();
            
            // Kiểm tra và ghi log số dòng bị ảnh hưởng
            if (rowsAffected > 0) {
                System.out.println("Server deleted successfully. Rows affected: " + rowsAffected);
            } else {
                System.out.println("No server found with IP " + serverIp + " and port " + port);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
 // Kiểm tra server có tồn tại không
    public boolean serverExists(String serverIp, int port) {
        String sql = "SELECT COUNT(*) FROM server_config WHERE server_ip = ? AND server_port = ?";
        
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
    
    // Phương thức lấy địa chỉ IP và Port của server
    public Server getServerIpAndPort() {
        String sql = "SELECT server_ip, server_port FROM server_config LIMIT 1"; // Lấy bản ghi đầu tiên
        Server server = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String serverIp = resultSet.getString("server_ip");
                int serverPort = resultSet.getInt("server_port");
                server = new Server(serverIp, serverPort);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving server IP and port: " + e.getMessage());
            e.printStackTrace();
        }

        return server;
    }

    // Phương thức lấy địa chỉ IP của server
    public String getServerIp() {
        String sql = "SELECT server_ip FROM server_config LIMIT 1"; // Lấy địa chỉ IP từ bản ghi đầu tiên
        String serverIp = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                serverIp = resultSet.getString("server_ip");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving server IP: " + e.getMessage());
            e.printStackTrace();
        }

        return serverIp;
    }
}