package controller;

import client.MailClient;
import lombok.Setter;
import service.EmailSenderService;
import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendEmailController {
    private EmailSenderService emailSenderService;

    @Setter
    private File[] attachments;
    public SendEmailController(MailClient client, String userEmail,EmailSenderService emailSenderService) {
        this.emailSenderService = new EmailSenderService(client, userEmail);

    }

    // Handle sending email
    public void sendEmail(String receiver, String subject, String content, JLabel statusLabel) {
        try {
            if (receiver.isEmpty() || subject.isEmpty() || content.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all fields.");
            }
            // Gửi email cùng hoặc không có tệp đính kèm
            String response = emailSenderService.sendEmail(receiver, subject, content, attachments != null && attachments.length > 0 ? attachments : null);
            statusLabel.setText("Email Sent: " + response);
            JOptionPane.showMessageDialog(null, "Email Sent: " + response, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    public void scheduleEmail(String receiver, String subject, String content, Date scheduledTime) {
        String scheduledDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scheduledTime);
        JOptionPane.showMessageDialog(null, "Email scheduled for: " + scheduledDateTime, "Scheduled", JOptionPane.INFORMATION_MESSAGE);
    }


}
