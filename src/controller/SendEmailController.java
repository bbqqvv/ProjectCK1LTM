package controller;

import client.MailClient;
import service.EmailSenderService;
import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendEmailController {

    private EmailSenderService emailSenderService;
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

            // Gửi email cùng với tệp đính kèm
            String response = emailSenderService.sendEmail(receiver, subject, content, attachments);
            statusLabel.setText("Email Sent: " + response);
            JOptionPane.showMessageDialog(null, response, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    // Handle scheduling email
    public void scheduleEmail(String receiver, String subject, String content, Date scheduledTime) {
        String scheduledDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scheduledTime);
        JOptionPane.showMessageDialog(null, "Email scheduled for: " + scheduledDateTime, "Scheduled", JOptionPane.INFORMATION_MESSAGE);
        // Future logic for scheduling can go here
    }

    // Handle file selection
    public String chooseFilesToAttach(JFileChooser fileChooser, JLabel fileNameLabel) {
        fileChooser.setMultiSelectionEnabled(true); // Cho phép chọn nhiều tệp
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            attachments = fileChooser.getSelectedFiles(); // Gán tệp đã chọn vào biến attachments
            if (attachments.length > 0) {
                StringBuilder fileNames = new StringBuilder("Selected: ");
                for (File file : attachments) {
                    fileNames.append(file.getName()).append(" ");
                }
                fileNameLabel.setText(fileNames.toString());
                return fileNames.toString();
            }
        }
        return "No files chosen";
    }


    public void setAttachments(File[] selectedFiles) {
    }
}
