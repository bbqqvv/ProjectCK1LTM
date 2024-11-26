package service;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;

import client.MailClient;

public class EmailSenderService {
    private MailClient client;
    private String userEmail;

    public EmailSenderService(MailClient client, String userEmail) {
        this.client = client;
        this.userEmail = userEmail;
    }

    /**
     * Gửi email đến server.
     *
     * @param receiver    Email người nhận.
     * @param subject     Tiêu đề email.
     * @param content     Nội dung email.
     * @param attachments Danh sách tệp đính kèm (có thể null).
     * @return Phản hồi từ server.
     * @throws Exception Nếu có lỗi xảy ra.
     */
    public String sendEmail(String receiver, String subject, String content, File[] attachments) throws Exception {
        String attachmentData = prepareAttachmentData(attachments);

        // Định dạng dữ liệu gửi đi
        String data = userEmail + ":" + receiver + ":" + subject + ":" + content + ":" + attachmentData;

        // Nếu có tệp đính kèm, chuyển sang TCP để gửi tệp
        boolean useTcp = attachments != null && attachments.length > 0;

        // Gửi lệnh "SEND_EMAIL" qua UDP hoặc "SEND_EMAIL_ATTACHMENT" qua TCP
        if (useTcp) {
            return client.sendRequest("SEND_EMAIL_ATTACHMENT", data, true,attachments);  // true để sử dụng TCP
        } else {
            return client.sendRequest("SEND_EMAIL", data, false,null); // false để sử dụng UDP
        }
    }


    /**
     * Lên lịch gửi email.
     *
     * @param receiver      Email người nhận.
     * @param subject       Tiêu đề email.
     * @param content       Nội dung email.
     * @param scheduledDate Thời gian gửi (dạng Date).
     * @return Phản hồi từ server.
     * @throws Exception Nếu có lỗi xảy ra.
     */
    public String scheduleEmail(String receiver, String subject, String content, Date scheduledDate) throws Exception {
        String formattedDate = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", scheduledDate);

        // Định dạng dữ liệu gửi đi
        String data = userEmail + ":" + receiver + ":" + subject + ":" + content + ":" + formattedDate;

        // Gửi lệnh "SCHEDULE_EMAIL" qua UDP
        return client.sendRequest("SCHEDULE_EMAIL", data, false,null);
    }

    /**
     * Chuẩn bị dữ liệu tệp đính kèm dưới dạng Base64.
     *
     * @param attachments Danh sách tệp (có thể null).
     * @return Chuỗi dữ liệu tệp đã mã hóa.
     * @throws Exception Nếu có lỗi trong quá trình đọc tệp.
     */
    private String prepareAttachmentData(File[] attachments) throws Exception {
        if (attachments == null || attachments.length == 0) {
            return ""; // Không có tệp đính kèm
        }

        StringBuilder attachmentData = new StringBuilder();
        for (File file : attachments) {
            String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            attachmentData.append(file.getName()).append(":").append(encodedFile).append(";");
        }
        return attachmentData.toString();
    }

}
