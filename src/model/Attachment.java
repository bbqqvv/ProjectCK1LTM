package model;

import java.time.LocalDateTime;

public class Attachment {
    private int id;            // ID của attachment
    private int mailId;        // ID của email mà attachment này thuộc về
    private String fileName;   // Tên tệp đính kèm
    private String filePath;   // Đường dẫn đến tệp đính kèm
    private LocalDateTime uploadedAt; // Thời gian tải lên (thời gian đính kèm vào email)

    // Constructor
    public Attachment(int id, int mailId, String fileName, String filePath, LocalDateTime uploadedAt) {
        this.id = id;
        this.mailId = mailId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMailId() {
        return mailId;
    }

    public void setMailId(int mailId) {
        this.mailId = mailId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // Phương thức toString() để in thông tin đối tượng ra chuỗi
    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", mailId=" + mailId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
