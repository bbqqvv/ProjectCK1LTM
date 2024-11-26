package model;

public class Attachment {
    private int attachmentId;    // ID của tệp đính kèm (Primary Key)
    private int mailId;          // ID của email (Foreign Key)
    private String fileName;     // Tên tệp
    private String filePath;     // Đường dẫn lưu trữ tệp
    private long fileSize;       // Kích thước tệp (byte)
    private String fileType;     // Loại tệp (MIME type)

    /**
     * Constructor không tham số.
     * Dành cho việc khởi tạo tạm thời và gán giá trị sau.
     */
    public Attachment() {
    }

    /**
     * Constructor đầy đủ tham số.
     *
     * @param attachmentId ID của tệp đính kèm
     * @param mailId       ID của email liên quan
     * @param fileName     Tên tệp
     * @param filePath     Đường dẫn lưu trữ tệp
     * @param fileSize     Kích thước tệp (byte)
     * @param fileType     Loại tệp (MIME type)
     */
    public Attachment(int attachmentId, int mailId, String fileName, String filePath, long fileSize, String fileType) {
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
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        if (fileSize < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }
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
        return "Attachment{" +
                "attachmentId=" + attachmentId +
                ", mailId=" + mailId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
