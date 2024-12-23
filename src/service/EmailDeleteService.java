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
     * @param userEmail Email của người dùng.
     * @param emailId   ID của email cần xóa.
     * @return Phản hồi từ server.
     * @throws Exception Nếu có lỗi trong quá trình gửi yêu cầu.
     */
    public String deleteEmail(String userEmail, String emailId) throws Exception {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalArgumentException("Email người dùng không được để trống.");
        }
        if (emailId == null || emailId.isEmpty()) {
            throw new IllegalArgumentException("ID email không được để trống.");
        }

        // Kiểm tra xem emailId có phải là số hay không
        if (!emailId.matches("\\d+")) {
            throw new IllegalArgumentException("Email ID phải là số nguyên.");
        }

        String data = "DELETE_EMAIL:" + userEmail + ":" + emailId;

        String response = client.sendRequest(data);

        if (response == null || response.isEmpty()) {
            throw new Exception("Server không phản hồi khi xóa email.");
        }

        if (response.equalsIgnoreCase("SUCCESS")) {
            return "Email với ID " + emailId + " đã được xóa thành công.";
        } else if (response.equalsIgnoreCase("NOT_FOUND")) {
            return "Không tìm thấy email với ID " + emailId + " để xóa.";
        } else {
            throw new Exception("Lỗi từ server: " + response);
        }
    }

}
