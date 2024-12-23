package service;

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
     * @param receiver Email người nhận.
     * @param subject  Tiêu đề email.
     * @param content  Nội dung email.
     * @return Phản hồi từ server.
     * @throws Exception Nếu có lỗi xảy ra.
     */
    public String sendEmail(String receiver, String subject, String content) throws Exception {
        if (receiver == null || receiver.isEmpty()) {
            throw new IllegalArgumentException("Email người nhận không được để trống.");
        }
        if (subject == null) subject = ""; // Tiêu đề rỗng nếu null
        if (content == null) content = ""; // Nội dung rỗng nếu null

        // Tạo dữ liệu gửi đi
        String data = "SEND_EMAIL:" + userEmail + ":" + receiver + ":" + subject + ":" + content;

        // Gửi dữ liệu đến server thông qua client
        return client.sendRequest(data);
    }
}
