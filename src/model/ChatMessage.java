package model;

import java.sql.Timestamp;

public class ChatMessage {
    private int messageId;
    private int chatId;
    private Integer senderId;  // Allows null value for deleted users
    private String message;
    private Timestamp sentAt;

    // Constructor
    public ChatMessage(int messageId, int chatId, Integer senderId, String message, Timestamp sentAt) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Getters and setters
    public int getMessageId() { return messageId; }
    public int getChatId() { return chatId; }
    public Integer getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public Timestamp getSentAt() { return sentAt; }
}
