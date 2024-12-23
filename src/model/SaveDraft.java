package model;

public class SaveDraft {
    private int userId;  // user_id
    private String receiver;
    private String subject;
    private String content;

    public SaveDraft(int userId, String receiver, String subject, String content) {
        this.userId = userId;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
    }
    public SaveDraft(String receiver, String subject, String content) {
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
    }

    public SaveDraft(String receiver, String subject, String content, int userId) {
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
