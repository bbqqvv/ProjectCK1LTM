package model;

import java.sql.Timestamp;

public class Chat {
    private int chatId;
    private boolean isGroupChat;
    private Timestamp createdAt;

    // Constructor
    public Chat(int chatId, boolean isGroupChat, Timestamp createdAt) {
        this.chatId = chatId;
        this.isGroupChat = isGroupChat;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getChatId() { return chatId; }
    public boolean isGroupChat() { return isGroupChat; }
    public Timestamp getCreatedAt() { return createdAt; }
}
