package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addUser(User user) {
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email, ip_address) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.getEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Ghi log và ném ngoại lệ
            throw new RuntimeException("Error adding user", e);
        }
    }


    public boolean loginUser(User loginUser) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, loginUser.getEmail());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    System.out.println("Stored Password: " + storedPassword); // Ghi log mật khẩu đã lưu
                    boolean isPasswordCorrect = BCrypt.checkpw(loginUser.getPassword(), storedPassword);
                    System.out.println("Password Check: " + isPasswordCorrect); // Kiểm tra mật khẩu
                    return isPasswordCorrect;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public void updateUserIpAddress(String username, String ipAddress) {
        String sql = "UPDATE users SET ip_address = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Các phương thức khác không thay đổi
}
