package model;

import java.sql.Timestamp;

public class ChatParticipant {
    private int chatId;
    private int userId;
    private Timestamp joinedAt;

    // Constructor
    public ChatParticipant(int chatId, int userId, Timestamp joinedAt) {
        this.chatId = chatId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    // Getters and setters
    public int getChatId() { return chatId; }
    public int getUserId() { return userId; }
    public Timestamp getJoinedAt() { return joinedAt; }
}
