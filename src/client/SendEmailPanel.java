package client;

import controller.SendEmailController;
import service.EmailSenderService;

import javax.swing.*;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.util.Date;

public class SendEmailPanel extends JPanel {

    private JTextField receiverField;
    private JTextField subjectField;
    private JTextArea contentArea;
    private JLabel fileNameLabel;
    private JButton scheduleButton;
    private JPanel schedulePanel;
    private SendEmailController sendEmailController;

    public SendEmailPanel(MailClientView parent) {
        MailClient client = parent.getClient();
        String userEmail = parent.getUserEmail();
        EmailSenderService emailSenderService = new EmailSenderService(client, userEmail);

        // Initialize the controller
        sendEmailController = new SendEmailController(emailSenderService);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input fields for receiver and subject
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Email Details"));

        receiverField = new JTextField(20);
        subjectField = new JTextField(20);

        inputPanel.add(new JLabel("Receiver Email:"));
        inputPanel.add(receiverField);
        inputPanel.add(new JLabel("Subject:"));
        inputPanel.add(subjectField);

        // Attachment panel
        JPanel attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attachmentPanel.setBackground(Color.WHITE);
        attachmentPanel.setBorder(BorderFactory.createTitledBorder("Attachment"));

        JButton attachButton = new JButton("Choose File");
        fileNameLabel = new JLabel("No file chosen");
        JFileChooser fileChooser = new JFileChooser();

        attachButton.addActionListener(e -> {
            String fileNames = sendEmailController.chooseFilesToAttach(fileChooser, fileNameLabel);
            fileNameLabel.setText(fileNames);
        });

        attachmentPanel.add(new JLabel("Attach File:"));
        attachmentPanel.add(attachButton);
        attachmentPanel.add(fileNameLabel);

        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder("Email Content"));

        contentArea = new JTextArea(10, 30);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton sendButton = new JButton("📧 Send Email");
        sendButton.addActionListener(e -> sendEmail());

        JButton clearButton = new JButton("🧹 Clear Fields");
        clearButton.addActionListener(e -> clearFields());

        // Schedule Button to show/hide the scheduling panel
        scheduleButton = new JButton("⏰ Schedule Send");
        scheduleButton.addActionListener(e -> toggleSchedulePanelVisibility());

        buttonPanel.add(sendButton);
        buttonPanel.add(scheduleButton);
        buttonPanel.add(clearButton);

        // Schedule Panel for date and time selection
        schedulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schedulePanel.setBackground(Color.WHITE);
        schedulePanel.setVisible(false); // Hide by default

        JLabel scheduleLabel = new JLabel("Select Date and Time:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss"));

        schedulePanel.add(scheduleLabel);
        schedulePanel.add(dateChooser);
        schedulePanel.add(timeSpinner);

        // Add components to the main panel
        add(inputPanel);
        add(Box.createVerticalStrut(10));
        add(attachmentPanel);
        add(Box.createVerticalStrut(10));
        add(contentPanel);
        add(Box.createVerticalStrut(10));
        add(schedulePanel); // Add schedule panel below content
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
    }

    private void toggleSchedulePanelVisibility() {
        // Show or hide the schedule panel when the button is clicked
        boolean isVisible = !schedulePanel.isVisible();
        schedulePanel.setVisible(isVisible);
        scheduleButton.setText(isVisible ? "❌ Cancel Schedule" : "⏰ Schedule Send"); // Change button text based on state
        revalidate(); // Re-layout the panel
        repaint();
    }

    private void sendEmail() {
        String receiver = receiverField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();
        JLabel statusLabel = new JLabel(); // Temporary status label for demo purposes
        sendEmailController.sendEmail(receiver, subject, content, statusLabel);
    }

    private void clearFields() {
        receiverField.setText("");
        subjectField.setText("");
        contentArea.setText("");
        fileNameLabel.setText("No file chosen");
    }
}
