package dao;

import model.SaveDraft;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaveDarftDao {
    private Connection connection;

    // Constructor để truyền Connection từ ngoài vào
    public SaveDarftDao(Connection connection) {
        this.connection = connection;
    }

    public int saveEmail(SaveDraft saveDraft) {
        String sql = "INSERT INTO save_email (user_id, receiver, subject, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Đảm bảo cung cấp giá trị cho user_id
            stmt.setInt(1, saveDraft.getUserId());  // Thêm user_id
            stmt.setString(2, saveDraft.getReceiver());  // Index 2 cho receiver
            stmt.setString(3, saveDraft.getSubject());   // Index 3 cho subject
            stmt.setString(4, saveDraft.getContent());   // Index 4 cho content
            return stmt.executeUpdate();  // Trả về số dòng ảnh hưởng (lớn hơn 0 nếu thành công)
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;  // Trả về -1 nếu có lỗi
        }
    }

    public List<SaveDraft> getAllMailsForUser(String userEmail) {
        List<SaveDraft> drafts = new ArrayList<>();
        String query = "SELECT * FROM save_email WHERE user_id = (SELECT id FROM users WHERE email = ?)";  // Lọc theo user_id

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userEmail);  // Lấy user_id của người dùng từ email

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SaveDraft draft = new SaveDraft(
                        rs.getString("receiver"),
                        rs.getString("subject"),
                        rs.getString("content"),
                        rs.getInt("user_id") // Giả sử SaveDraft có trường user_id
                );
                drafts.add(draft);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return drafts;
    }



    // Phương thức chuyển danh sách Save thành chuỗi để hiển thị
    private String convertMailsToString(List<SaveDraft> saveDraftList) {
        StringBuilder sb = new StringBuilder();
        for (SaveDraft saveDraft : saveDraftList) {
            sb.append("Receiver: ").append(saveDraft.getReceiver())
                    .append(", Subject: ").append(saveDraft.getSubject())
                    .append("\nContent: ").append(saveDraft.getContent())
                    .append("\n\n");  // Ngắt dòng giữa các email
        }
        return sb.toString();
    }
}
