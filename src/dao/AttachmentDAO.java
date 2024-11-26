package dao;

import model.Attachment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    /**
     * Thêm nhiều tệp đính kèm cho một email.
     * @param attachments Danh sách tệp đính kèm cần thêm.
     * @return true nếu tất cả tệp được thêm thành công, false nếu có lỗi.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình thêm.
     */
    public boolean addAttachments(List<Attachment> attachments) throws SQLException {
        if (attachments == null || attachments.isEmpty()) {
            throw new IllegalArgumentException("Attachments list cannot be null or empty.");
        }

        String query = "INSERT INTO attachments (mail_id, file_name, file_path, file_size, file_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (Attachment attachment : attachments) {
                ps.setInt(1, attachment.getMailId());
                ps.setString(2, attachment.getFileName());
                ps.setString(3, attachment.getFilePath());

                // Kiểm tra file trước khi thêm
                File file = new File(attachment.getFilePath());
                if (!file.exists() || !file.isFile()) {
                    throw new IllegalArgumentException("File not found: " + attachment.getFilePath());
                }

                ps.setLong(4, file.length());
                ps.setString(5, Files.probeContentType(Paths.get(attachment.getFilePath())));
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            for (int result : results) {
                if (result == Statement.EXECUTE_FAILED) {
                    return false; // Một tệp không được thêm thành công
                }
            }
            return true; // Tất cả tệp được thêm thành công
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lấy danh sách tệp đính kèm theo mail_id.
     * @param mailId ID của email cần lấy tệp đính kèm.
     * @return Danh sách tệp đính kèm.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình truy vấn.
     */
    public List<Attachment> getAttachmentsByMailId(int mailId) throws SQLException {
        if (mailId <= 0) {
            throw new IllegalArgumentException("Invalid mail ID: " + mailId);
        }

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

    /**
     * Xóa tất cả tệp đính kèm theo mail_id.
     * @param mailId ID của email cần xóa tệp đính kèm.
     * @return true nếu xóa thành công, false nếu không.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình xóa.
     */
    public boolean deleteAttachmentsByMailId(int mailId) throws SQLException {
        if (mailId <= 0) {
            throw new IllegalArgumentException("Invalid mail ID: " + mailId);
        }

        String query = "DELETE FROM attachments WHERE mail_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chuyển đổi ResultSet thành đối tượng Attachment.
     * @param rs ResultSet cần chuyển đổi.
     * @return Đối tượng Attachment.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình truy cập dữ liệu.
     */
    private Attachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        return new Attachment(
                rs.getInt("attachment_id"),
                rs.getInt("mail_id"),
                rs.getString("file_name"),
                rs.getString("file_path"),
                rs.getLong("file_size"),
                rs.getString("file_type")
        );
    }
}
