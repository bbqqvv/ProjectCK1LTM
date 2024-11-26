package client;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

import controller.SendEmailController;
import service.EmailSenderService;

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
        sendEmailController = new SendEmailController(client,userEmail,emailSenderService);

        // Panel layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(255, 255, 255));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Input fields for receiver and subject
        JPanel inputPanel = createTitledPanel("Email Details", new GridLayout(2, 2, 10, 10));
        receiverField = new JTextField(25);
        subjectField = new JTextField(25);

        inputPanel.add(new JLabel("Receiver Email:"));
        inputPanel.add(receiverField);
        inputPanel.add(new JLabel("Subject:"));
        inputPanel.add(subjectField);

        // Attachment panel
        JPanel attachmentPanel = createTitledPanel("Attachment", new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton attachButton = new JButton("📂 Choose File");
        attachButton.setToolTipText("Click to choose a file");
        fileNameLabel = new JLabel("No file chosen");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true); // Cho phép chọn nhiều tệp

        attachButton.addActionListener(e -> {
            String result = sendEmailController.chooseFilesToAttach(fileChooser, fileNameLabel);
            JOptionPane.showMessageDialog(this, result, "Attachment Status", JOptionPane.INFORMATION_MESSAGE);
        });


        attachmentPanel.add(attachButton);
        attachmentPanel.add(fileNameLabel);

        // Content area
        JPanel contentPanel = createTitledPanel("Email Content", new BorderLayout());
        contentArea = new JTextArea(8, 30);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(255, 255, 255));

        JButton sendButton = new JButton("📧 Send Email");
        sendButton.addActionListener(e -> sendEmail());
        sendButton.setBackground(new Color(72, 209, 204));
        sendButton.setForeground(Color.WHITE);

        JButton clearButton = new JButton("🧹 Clear Fields");
        clearButton.addActionListener(e -> clearFields());
        clearButton.setBackground(new Color(255, 99, 71));
        clearButton.setForeground(Color.WHITE);

        scheduleButton = new JButton("⏰ Schedule Send");
        scheduleButton.addActionListener(e -> toggleSchedulePanelVisibility());
        scheduleButton.setBackground(new Color(255, 255, 255));
        scheduleButton.setForeground(Color.WHITE);

        buttonPanel.add(sendButton);
        buttonPanel.add(scheduleButton);
        buttonPanel.add(clearButton);

        // Schedule Panel for date and time selection
        schedulePanel = createTitledPanel("Schedule Email", new FlowLayout(FlowLayout.LEFT, 10, 5));
        schedulePanel.setVisible(false); // Hidden by default

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
        add(schedulePanel);
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
    }

    private JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1, true),
                title
        ));
        return panel;
    }

    private void toggleSchedulePanelVisibility() {
        boolean isVisible = !schedulePanel.isVisible();
        schedulePanel.setVisible(isVisible);
        scheduleButton.setText(isVisible ? "❌ Cancel Schedule" : "⏰ Schedule Send");
        revalidate();
        repaint();
    }

    private void sendEmail() {
        String receiver = receiverField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();
        JLabel statusLabel = new JLabel(); // Hiển thị trạng thái gửi

        sendEmailController.sendEmail(receiver, subject, content, statusLabel);
        JOptionPane.showMessageDialog(this, statusLabel.getText(), "Status", JOptionPane.INFORMATION_MESSAGE);
    }
    private void clearFields() {
        receiverField.setText("");
        subjectField.setText("");
        contentArea.setText("");
        fileNameLabel.setText("No file chosen");
    }


    public void setReceiver(String receiver) {
        receiverField.setText(receiver);
    }

    public void setSubject(String subject) {
        subjectField.setText(subject);
    }

    public void setContent(String content) {
        contentArea.setText(content);
    }

}
