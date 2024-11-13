package dao;

import java.sql.*;

import model.Chat;

public class ChatDAO {
    private Connection connection;

    public ChatDAO(Connection connection) {
        this.connection = connection;
    }

    public Chat createChat(boolean isGroupChat) throws SQLException {
        String sql = "INSERT INTO chat (is_group_chat) VALUES (?)";
        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setBoolean(1, isGroupChat);
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            int chatId = rs.getInt(1);
            Timestamp createdAt = new Timestamp(System.currentTimeMillis());
            return new Chat(chatId, isGroupChat, createdAt);
        }
        return null;
    }

    public Chat getChatById(int chatId) throws SQLException {
        String sql = "SELECT * FROM chat WHERE chat_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, chatId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            boolean isGroupChat = rs.getBoolean("is_group_chat");
            Timestamp createdAt = rs.getTimestamp("created_at");
            return new Chat(chatId, isGroupChat, createdAt);
        }
        return null;
    }

    // Other methods like updateChat, deleteChat, etc. can be added as needed
}
