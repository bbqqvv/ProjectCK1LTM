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
							rs.getString("subject"), rs.getString("content"), rs.getDate("sent_date"),
							rs.getBoolean("is_sent"));
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
			sb.append("ID: ").append(mail.getId()).append(", Sender: ").append(mail.getSender()).append(", Receiver: ")
					.append(mail.getReceiver()).append(", Subject: ").append(mail.getSubject()).append(", Content: ")
					.append(mail.getContent()).append(", Sent Date: ").append(mail.getSentDate()).append(", Is Sent: ")
					.append(mail.isSent()).append("\n"); // Dùng "\n" để ngắt dòng giữa các email
		}
		return sb.toString();
	}

	public String searchMailsForUser(String email, String keyword, int currentPage, int emailsPerPage) {
		List<Mail> mails = new ArrayList<>();

		// Câu lệnh SQL tìm kiếm email
		String sql = "SELECT * FROM mails WHERE receiver = ? AND (subject LIKE ? OR content LIKE ?) LIMIT ?, ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			// Set các tham số cho câu lệnh SQL
			stmt.setString(1, email); // Địa chỉ email người nhận
			stmt.setString(2, "%" + keyword + "%"); // Tìm kiếm trong subject
			stmt.setString(3, "%" + keyword + "%"); // Tìm kiếm trong content
			stmt.setInt(4, (currentPage - 1) * emailsPerPage); // Xác định điểm bắt đầu của trang hiện tại
			stmt.setInt(5, emailsPerPage); // Số lượng email trên mỗi trang

			// Thực thi câu lệnh truy vấn
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Mail mail = new Mail(rs.getInt("id"), rs.getString("sender"), rs.getString("receiver"),
							rs.getString("subject"), rs.getString("content"), rs.getDate("sent_date"),
							rs.getBoolean("is_sent"));
					mails.add(mail);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Chuyển danh sách email thành chuỗi và trả về
		return convertMailsToString(mails);
	}

	public boolean deleteMail(int mailId) {
	    String sql = "DELETE FROM mails WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setInt(1, mailId); // Set id của email cần xóa
	        int rowsAffected = stmt.executeUpdate();
	        
	        // Ghi log số lượng bản ghi bị ảnh hưởng
	        System.out.println("Rows affected: " + rowsAffected);  // Số dòng bị xóa

	        return rowsAffected > 0; // Nếu có ít nhất một dòng bị xóa, trả về true
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false; // Nếu có lỗi xảy ra, trả về false
	    }
	}


	public boolean mailExists(int mailId) {
	    String sql = "SELECT COUNT(*) FROM mails WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setInt(1, mailId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0;  // Nếu đếm được ít nhất 1 bản ghi, trả về true
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;  // Nếu không tìm thấy bản ghi nào, trả về false
	}
	
	public boolean markEmailAsRead(int mailId) {
	    String sql = "UPDATE mails SET is_sent = true WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setInt(1, mailId); // Set ID của email cần cập nhật
	        int rowsAffected = stmt.executeUpdate();
	        
	        // Kiểm tra xem có bản ghi nào bị ảnh hưởng không
	        return rowsAffected > 0; // Trả về true nếu có ít nhất một bản ghi bị thay đổi
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false; // Nếu có lỗi xảy ra, trả về false
	    }
	}


}