package dao;

import model.Attachment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttachmentDAO {
    private final Connection connection;

    public AttachmentDAO(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
        this.connection = connection;
    }
    // Thêm nhiều tệp đính kèm cho một email
    public boolean addAttachments(List<Attachment> attachments) throws SQLException {
        String query = "INSERT INTO attachments (mail_id, file_name, file_path, file_size, file_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (Attachment attachment : attachments) {
                ps.setInt(1, attachment.getMailId());
                ps.setString(2, attachment.getFileName());
                ps.setString(3, attachment.getFilePath());
                ps.setObject(4, attachment.getFileSize(), Types.INTEGER);
                ps.setString(5, attachment.getFileType());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            return results.length == attachments.size(); // Kiểm tra tất cả tệp được thêm thành công
        }
    }


    // Lấy danh sách tệp đính kèm theo mail_id
    public List<Attachment> getAttachmentsByMailId(int mailId) throws SQLException {
        List<Attachment> attachments = new ArrayList<>();
        String query = "SELECT * FROM attachments WHERE mail_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, mailId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapResultSetToAttachment(rs));
                }
            }
        }
        return attachments;
    }

    // Phương thức trợ giúp: Chuyển đổi ResultSet thành đối tượng Attachment
    private Attachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        return new Attachment(
                rs.getInt("attachment_id"),
                rs.getInt("mail_id"),
                rs.getString("file_name"),
                rs.getString("file_path"),
                rs.getObject("file_size", Integer.class),
                rs.getString("file_type")
        );
    }

    // Các hàm CRUD khác không thay đổi
}
