package dao;

import model.Server;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import common.SnowflakeIdWorker;

public class ServerDAO {
    private Connection connection;
	private SnowflakeIdWorker idWorker;

    public ServerDAO(Connection connection) {
        this.connection = connection;
        this.idWorker = new SnowflakeIdWorker(1, 1); 
    }

    public void saveServer(String serverIp, int serverPort) {
        if (serverExists(serverIp, serverPort)) {
            System.out.println("Server with IP " + serverIp + " and Port " + serverPort + " already exists.");
            return; 
        }

        long uniqueId = idWorker.generateId();  // Tạo ID duy nhất chỉ một lần

        String sql = "INSERT INTO server_config (server_id, server_ip, server_port) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Sử dụng Snowflake ID (kiểu long) thay vì UUID
            preparedStatement.setLong(1, uniqueId);  // Thay vì set String cho server_id, sử dụng setLong
            preparedStatement.setString(2, serverIp); 
            preparedStatement.setInt(3, serverPort); 
            preparedStatement.executeUpdate(); 
            System.out.println("Server saved successfully with IP " + serverIp + " and Port " + serverPort);
        } catch (SQLException e) {
            System.err.println("Error saving server: " + e.getMessage());
            e.printStackTrace();
            // Nên thêm log lỗi vào hệ thống thay vì chỉ in ra console
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
            // Nên có log lỗi ở đây
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