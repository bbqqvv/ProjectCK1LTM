package dao;

import java.sql.*;

public class ChatParticipantDAO {
    private Connection connection;

    public ChatParticipantDAO(Connection connection) {
        this.connection = connection;
    }

    public void addParticipant(int chatId, int userId) throws SQLException {
        String sql = "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, chatId);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    public void removeParticipant(int chatId, int userId) throws SQLException {
        String sql = "DELETE FROM chat_participants WHERE chat_id = ? AND user_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, chatId);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }
}
