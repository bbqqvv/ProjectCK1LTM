package service;

import client.MailClient;

public class EmailDeleteService {
    private MailClient client;

    public EmailDeleteService(MailClient client) {
        this.client = client;
    }

    /**
     * Xóa email dựa trên userEmail và emailId.
     *
     * @param userEmail Email người dùng.
     * @param emailId   ID của email cần xóa.
     * @return Phản hồi từ server.
     * @throws Exception Nếu có lỗi trong quá trình gửi yêu cầu.
     */
    public String deleteEmail(String userEmail, String emailId) throws Exception {
        // Chuẩn bị dữ liệu xóa email
        String data = userEmail + ":" + emailId;

        // Gửi yêu cầu đến server với lệnh "DELETE_EMAIL"
        return client.sendRequest("DELETE_EMAIL", data, false,null); // Sử dụng UDP
    }
}
