package dao;

import model.Mail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import database.DatabaseConnection;

public class MailDAO {

    // Phương thức lưu email vào cơ sở dữ liệu
    public boolean saveEmail(String sender, String receiver, String subject, String content) {
        String query = "INSERT INTO mails (sender, receiver, subject, content, sent_date, is_sent) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, subject);
            stmt.setString(4, content);
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now())); // Lưu ngày giờ hiện tại
            stmt.setBoolean(6, false); // Mặc định trạng thái là chưa gửi
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức tải email cho người dùng
    public String loadEmails(String username) {
        StringBuilder emails = new StringBuilder();
        String query = "SELECT * FROM mails WHERE receiver = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                emails.append("From: ").append(rs.getString("sender")).append("\n");
                emails.append("Subject: ").append(rs.getString("subject")).append("\n");
                emails.append("Content: ").append(rs.getString("content")).append("\n");
                emails.append("Sent Date: ").append(rs.getTimestamp("sent_date")).append("\n");
                emails.append("Is Sent: ").append(rs.getBoolean("is_sent") ? "Yes" : "No").append("\n\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emails.toString();
    }
}
