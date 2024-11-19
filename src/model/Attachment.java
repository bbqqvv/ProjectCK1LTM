package model;

public class Attachment {
    private int attachmentId;
    private int mailId;
    private String fileName;
    private String filePath;
    private Integer fileSize;
    private String fileType;

    // Constructor không tham số
    public Attachment() {
    }

    // Constructor đầy đủ tham số (không bao gồm uploadedAt)
    public Attachment(int attachmentId, int mailId, String fileName, String filePath, Integer fileSize, String fileType) {
        this.attachmentId = attachmentId;
        this.mailId = mailId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    // Getter và Setter
    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
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

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "Attachments{" +
                "attachmentId=" + attachmentId +
                ", mailId=" + mailId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
