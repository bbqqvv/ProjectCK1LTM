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
        String sql = "INSERT INTO users (username, password, email, ip_address,port_address, is_login) VALUES (?, ?, ?, NULL, NULL, 0)";
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

    public void updateUserIpAddress(String email, String ipAddress, int portAddress) {
        // Updated SQL query to include the port_address column
        String sql = "UPDATE users SET ip_address = ?, port_address = ? WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the parameters: IP address, port address, and email
            stmt.setString(1, ipAddress);
            stmt.setInt(2, portAddress);  // Set port address as an integer
            stmt.setString(3, email);     // Set the email to match the user

            // Execute the update
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User IP and port address updated successfully.");
            } else {
                System.out.println("No user found with the provided email.");
            }
        } catch (SQLException e) {
            // Handle SQL exceptions
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
    public String getUserPortAddress(String receiverEmail) {
        String sql = "SELECT port_address FROM users WHERE email = ?";
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
    public String[] getUserIpAndPort(String email) throws SQLException {
        String sql = "SELECT ip_address, port_address FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String ipAddress = rs.getString("ip_address");
                    String portAddress = rs.getString("port_address");
                    return new String[] { ipAddress, portAddress };
                }
            }
        }
        return null; // Trả về null nếu không tìm thấy thông tin của người nhận
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