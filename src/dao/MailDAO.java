package dao;

import model.Mail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MailDAO {
    private Connection connection;

    public MailDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addMail(Mail mail) {
        String sql = "INSERT INTO mails (sender, receiver, subject, content, sent_date, is_sent) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mail.getSender());
            stmt.setString(2, mail.getReceiver());
            stmt.setString(3, mail.getSubject());
            stmt.setString(4, mail.getContent());
            stmt.setDate(5, new java.sql.Date(mail.getSentDate().getTime()));
            stmt.setBoolean(6, mail.isSent());
            stmt.executeUpdate();
            return true; // Trả về true nếu thêm thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi
        }
    }

    public String getAllMailsForUser(String username) {
        List<Mail> mails = new ArrayList<>();
        String sql = "SELECT * FROM mails WHERE receiver = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Mail mail = new Mail(rs.getInt("id"), rs.getString("sender"), rs.getString("receiver"),
                            rs.getString("subject"), rs.getString("content"),
                            rs.getDate("sent_date"), rs.getBoolean("is_sent"));
                    mails.add(mail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return convertMailsToString(mails);
    }

    private String convertMailsToString(List<Mail> mails) {
        StringBuilder sb = new StringBuilder();
        for (Mail mail : mails) {
            sb.append("ID: ").append(mail.getId())
              .append(", Sender: ").append(mail.getSender())
              .append(", Receiver: ").append(mail.getReceiver())
              .append(", Subject: ").append(mail.getSubject())
              .append(", Content: ").append(mail.getContent())
              .append(", Sent Date: ").append(mail.getSentDate())
              .append(", Is Sent: ").append(mail.isSent())
              .append("\n"); // Dùng "\n" để ngắt dòng giữa các email
        }
        return sb.toString();
    }
}