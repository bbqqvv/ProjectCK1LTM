package model;

import java.util.Date;
import java.util.List;

public class Mail {
    private int id;
    private String sender;
    private String receiver;
    private String subject;
    private String content;
    private Date sentDate;
    private boolean isSent;
    private List<Attachment> attachments; // Danh sách tệp đính kèm

    public Mail(int id, String sender, String receiver, String subject, String content, Date sentDate, boolean isSent) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.sentDate = sentDate;
        this.isSent = isSent;
    }

    // Constructor không tham số
    public Mail() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }


    // Constructor, getter, setter
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }
}