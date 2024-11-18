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

    public void saveServer(String serverIp, int serverPort) {
        if (serverExists(serverIp, serverPort)) {
            System.out.println("Server with IP " + serverIp + " and Port " + serverPort + " already exists.");
            return; 
        }

        String uniqueId = UUID.randomUUID().toString();  // Tạo UUID thay vì Snowflake ID

        String sql = "INSERT INTO server_config (server_id, server_ip, server_port) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Sử dụng UUID (kiểu String) thay vì Snowflake ID (kiểu long)
            preparedStatement.setString(1, uniqueId);  // Set UUID dưới dạng String
            preparedStatement.setString(2, serverIp); 
            preparedStatement.setInt(3, serverPort); 
            preparedStatement.executeUpdate(); 
            System.out.println("Server saved successfully with IP " + serverIp + " and Port " + serverPort);
        } catch (SQLException e) {
            System.err.println("Error saving server: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    public boolean deleteServer(String serverIp, int serverPort) {
        if (!serverExists(serverIp, serverPort)) {
            System.out.println("Server with IP " + serverIp + " and Port " + serverPort + " does not exist in the database.");
            return false; 
        }

        String sql = "DELETE FROM server_config WHERE server_ip = ? AND server_port = ?";
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

    public int getServerPort() {
        String sql = "SELECT server_port FROM server_config LIMIT 1"; // Lấy Port từ bản ghi đầu tiên
        int serverPort = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                serverPort = resultSet.getInt("server_port");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving server Port: " + e.getMessage());
            e.printStackTrace();
        }

        return serverPort;
    }
}
