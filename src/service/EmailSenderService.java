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

    // Phương thức gửi email
    public String sendEmail(String receiver, String subject, String content, File[] attachments) throws Exception {
        StringBuilder attachmentData = new StringBuilder();
        if (attachments != null) {
            for (File file : attachments) {
                String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                attachmentData.append(file.getName()).append(":").append(encodedFile).append(";");
            }
        }
        return client.sendRequest("SEND_EMAIL:" + userEmail + ":" + receiver + ":" + subject + ":" + content + ":" + attachmentData);
    }

	public String scheduleEmail(String receiver, String subject, String content, Date scheduledDate) {
		// TODO Auto-generated method stub
		return null;
	}
}
