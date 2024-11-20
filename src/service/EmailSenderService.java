package service;

import java.util.Date;

import client.MailClient;

public class EmailSenderService {
    private MailClient client;
    private String userEmail;

    public EmailSenderService(MailClient client, String userEmail) {
        this.client = client;
        this.userEmail = userEmail;
    }

    // Phương thức gửi email
    public String sendEmail(String receiver, String subject, String content) throws Exception {
        // Kiểm tra định dạng email (cơ bản)
        if (!receiver.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        // Giả lập gửi email
        return client.sendRequest("SEND_EMAIL:" + userEmail + ":" + receiver + ":" + subject + ":" + content);
    }

	public String scheduleEmail(String receiver, String subject, String content, Date scheduledDate) {
		// TODO Auto-generated method stub
		return null;
	}
}
