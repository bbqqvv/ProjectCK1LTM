package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.ChatMessage;

public class ChatMessageDAO {
    private Connection connection;

    public ChatMessageDAO(Connection connection) {
        this.connection = connection;
    }

    public ChatMessage sendMessage(int chatId, int senderId, String message) throws SQLException {
        String sql = "INSERT INTO chat_message (chat_id, sender_id, message) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, chatId);
            stmt.setInt(2, senderId);
            stmt.setString(3, message);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int messageId = rs.getInt(1);
                Timestamp sentAt = new Timestamp(System.currentTimeMillis());
                return new ChatMessage(messageId, chatId, senderId, message, sentAt);
            }
        } finally {
            if (stmt != null) stmt.close();
            if (rs != null) rs.close();
        }
        return null;
    }


    public List<ChatMessage> getMessagesByChatId(int chatId) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat_message WHERE chat_id = ? ORDER BY sent_at";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, chatId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                int messageId = rs.getInt("message_id");
                int senderId = rs.getInt("sender_id");
                String message = rs.getString("message");
                Timestamp sentAt = rs.getTimestamp("sent_at");
                messages.add(new ChatMessage(messageId, chatId, senderId, message, sentAt));
            }
        } finally {
            if (stmt != null) stmt.close();
            if (rs != null) rs.close();
        }
        return messages;
    }

}
