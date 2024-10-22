package model;

import java.time.LocalDateTime;

public class Mail {
    private String sender;
    private String receiver;
    private String content;
    private String subject; // Tiêu đề của email
    private LocalDateTime sentDate; // Ngày gửi
    private boolean isSent; // Trạng thái gửi

    public Mail(String sender, String receiver, String content, String subject) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.subject = subject;
        this.sentDate = LocalDateTime.now(); // Gán ngày gửi khi tạo
        this.isSent = false; // Mặc định là chưa gửi
    }

    // Getters
    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public boolean isSent() {
        return isSent;
    }

    // Setters
    public void setSent(boolean sent) {
        isSent = sent;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", subject='" + subject + '\'' +
                ", sentDate=" + sentDate +
                ", isSent=" + isSent +
                '}';
    }
}
