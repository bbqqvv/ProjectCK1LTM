package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import model.User;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addUser(User user) {
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email, ip_address, is_login) VALUES (?, ?, ?, NULL, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.getEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public int getUserIdByEmail(String email) throws SQLException {
        String query = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("User not found with email: " + email);
            }
        }
    }
    public boolean loginUser(User loginUser) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, loginUser.getEmail());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    boolean isPasswordCorrect = BCrypt.checkpw(loginUser.getPassword(), storedPassword);
                    
                    if (isPasswordCorrect) {
                        // Successful login, update is_login flag to 1
                        updateLoginStatus(loginUser.getEmail(), true); // Set is_login = 1
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void updateLoginStatus(String email, boolean isLogin) {
        String sql = "UPDATE users SET is_login = ? WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, isLogin ? 1 : 0); // 1 = logged in, 0 = logged out
            preparedStatement.setString(2, email);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Login status updated successfully for user: " + email);
            } else {
                System.out.println("No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.err.println("Error updating login status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean isEmailExist(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;  // Nếu email đã tồn tại, trả về true
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Nếu email chưa tồn tại, trả về false
    }

	public String getUsername(String email) {
        String sql = "SELECT username FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if the username is not found
    }

    public void updateUserIpAddress(String email, String ipAddress) {
        String sql = "UPDATE users SET ip_address = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ipAddress);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUserIpAddress(String receiverEmail) {
        String sql = "SELECT ip_address FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, receiverEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ip_address");  // Trả về địa chỉ IP của người nhận
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Nếu không tìm thấy, trả về null
    }


    public boolean isUserLoggedIn(String email) {
        String sql = "SELECT is_login FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_login");  // Trả về true nếu is_login = 1
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Trả về false nếu người dùng không có trong cơ sở dữ liệu
    }


    // Other methods remain unchanged
}